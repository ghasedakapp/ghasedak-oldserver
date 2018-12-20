package ir.sndu.server.rpc.auth.helper

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }

import im.ghasedak.api.auth.ApiAuth
import im.ghasedak.api.user.ApiUser
import ir.sndu.persist.repo.auth.{ AuthSessionRepo, AuthTransactionRepo, GateAuthCodeRepo }
import ir.sndu.persist.repo.user.{ UserPhoneRepo, UserRepo }
import ir.sndu.server.model.auth.{ AuthPhoneTransaction, AuthSession, AuthTransactionBase }
import ir.sndu.server.model.user.{ User, UserPhone }
import ir.sndu.server.rpc.auth.{ AuthRpcErrors, AuthServiceImpl }
import ir.sndu.server.utils.IdUtils._
import ir.sndu.server.utils.StringUtils._
import ir.sndu.server.utils.number.PhoneCodeGen.genPhoneCode
import ir.sndu.server.utils.number.PhoneNumberUtils._

trait AuthServiceHelper {
  this: AuthServiceImpl ⇒

  private val maximumValidCodeMinutes = 15 // for 15 minutes phone code is valid

  protected def forbidDeletedUser(userId: Int): Result[Unit] =
    fromDBIOBoolean(AuthRpcErrors.UserIsDeleted)(UserRepo.isDeleted(userId).map(!_))

  protected def getOptAuthTransactionWithExpire(optAuthTransaction: Option[AuthTransactionBase]): Result[Option[AuthTransactionBase]] = {
    optAuthTransaction match {
      case Some(transaction) ⇒
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val until = transaction.createdAt.until(now, ChronoUnit.MINUTES)
        for {
          _ ← if (until > maximumValidCodeMinutes)
            fromDBIO(AuthTransactionRepo.delete(transaction.transactionHash))
          else
            fromDBIO(AuthTransactionRepo.updateCreateAt(transaction.transactionHash, now))
          newTransaction = if (until > maximumValidCodeMinutes) None else Some(transaction)
        } yield newTransaction
      case None ⇒ point(optAuthTransaction)
    }
  }

  protected def sendSmsCode(phoneNumber: Long, transactionHash: String): Result[Unit] = {
    val codeHash = genPhoneCode(phoneNumber)
    for {
      _ ← fromDBIO(GateAuthCodeRepo.createOrUpdate(transactionHash, codeHash))
      // todo: send sms code here
    } yield ()
  }

  protected def validateCode(transaction: AuthTransactionBase, code: String): Result[Unit] = {
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val until = transaction.createdAt.until(now, ChronoUnit.MINUTES)
    for {
      gateAuthCode ← fromDBIOOption(AuthRpcErrors.AuthCodeExpired)(GateAuthCodeRepo.find(transaction.transactionHash))
      _ ← fromBoolean(AuthRpcErrors.InvalidAuthCode)(gateAuthCode.codeHash == code)
      _ ← fromBoolean(AuthRpcErrors.AuthCodeExpired)(until < maximumValidCodeMinutes)
      _ ← fromDBIO(AuthTransactionRepo.updateSetChecked(transaction.transactionHash))
    } yield ()
  }

  protected def getOptApiAuth(transaction: AuthPhoneTransaction, optUserPhone: Option[UserPhone]): Result[Option[ApiAuth]] = {
    optUserPhone match {
      case None ⇒ point(None)
      case Some(userPhone) ⇒
        for {
          userOpt ← fromDBIO(UserRepo.find(userPhone.userId))
          // todo: fix this (delete account)
          user = userOpt.get
          generatedToken ← fromFuture(generateToken(user.id))
          (tokenId, token) = generatedToken
          authSession = AuthSession(
            user.id, tokenId, transaction.appId, transaction.apiKey,
            transaction.deviceHash, transaction.deviceInfo, LocalDateTime.now(ZoneOffset.UTC))
          _ ← fromDBIO(AuthSessionRepo.create(authSession))
          _ ← fromDBIO(AuthTransactionRepo.delete(transaction.transactionHash))
          apiUser = ApiUser(user.id, user.name, None, user.nickname, Some(userPhone.number))
        } yield Some(ApiAuth(token, Some(apiUser)))
    }
  }

  protected def newUserPhoneSignUp(transaction: AuthPhoneTransaction, name: String): Result[Option[ApiAuth]] = {
    val phone = transaction.phoneNumber
    for {
      phoneAndCode ← fromOption(AuthRpcErrors.InvalidPhoneNumber)(normalizeWithCountry(phone).headOption)
      (_, countryCode) = phoneAndCode
      validName ← fromOption(AuthRpcErrors.InvalidName)(validName(name))
      user = User(
        id = nextIntId(),
        name = validName,
        countryCode = countryCode,
        createdAt = LocalDateTime.now(ZoneOffset.UTC))
      _ ← fromDBIO(UserRepo.create(user))
      userPhone = UserPhone(user.id, phone)
      _ ← fromDBIO(UserPhoneRepo.create(userPhone))
      optApiAuth ← getOptApiAuth(transaction, Some(userPhone))
    } yield optApiAuth
  }

}
