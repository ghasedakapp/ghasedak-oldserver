package ir.sndu.server.rpc.auth.helper

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }

import im.ghasedak.api.auth.ApiAuth
import im.ghasedak.api.user.ApiUser
import ir.sndu.persist.repo.auth.{ AuthSessionRepo, AuthTransactionRepo, GateAuthCodeRepo }
import ir.sndu.persist.repo.org.ApiKeyRepo
import ir.sndu.persist.repo.user.{ UserAuthRepo, UserRepo }
import ir.sndu.server.model.auth.{ AuthPhoneTransaction, AuthSession, AuthTransactionBase }
import ir.sndu.server.model.org.ApiKey
import ir.sndu.server.model.user.{ User, UserAuth }
import ir.sndu.server.rpc.auth.{ AuthRpcErrors, AuthServiceImpl }
import ir.sndu.server.rpc.common.CommonRpcErrors
import ir.sndu.server.user.UserUtils
import ir.sndu.server.utils.CodeGen.genPhoneCode
import ir.sndu.server.utils.StringUtils._
import ir.sndu.server.utils.number.IdUtils._
import ir.sndu.server.utils.number.PhoneNumberUtils._

trait AuthServiceHelper {
  this: AuthServiceImpl ⇒

  private val maximumValidCodeMinutes = 15 // for 15 minutes phone code is valid

  protected def getApiKey(apiKey: String): Result[Option[ApiKey]] = {
    AuthSession.isOfficialApiKey(apiKey) match {
      case Some(ak) ⇒ point(Some(ak))
      case None     ⇒ fromDBIO(ApiKeyRepo.find(apiKey))
    }
  }

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

  protected def getOptApiAuth(transaction: AuthTransactionBase, optUserAuth: Option[UserAuth]): Result[Option[ApiAuth]] = {
    optUserAuth match {
      case None ⇒ point(None)
      case Some(userAuth) ⇒
        for {
          userOpt ← fromDBIO(UserRepo.find(userAuth.userId))
          // todo: fix this (delete account)
          user = userOpt.get
          generatedToken ← fromFuture(generateToken(user.id, transaction.orgId))
          (tokenId, token) = generatedToken
          authSession = AuthSession(
            transaction.orgId, transaction.apiKey, user.id,
            tokenId, LocalDateTime.now(ZoneOffset.UTC))
          _ ← fromDBIO(AuthSessionRepo.create(authSession))
          _ ← fromDBIO(AuthTransactionRepo.delete(transaction.transactionHash))
          contactsRecord ← fromDBIO(UserUtils.getUserContactsRecord(user.id))
          apiUser = ApiUser(user.id, user.name, user.name, user.about, contactsRecord)
        } yield Some(ApiAuth(token, Some(apiUser)))
    }
  }

  protected def newUserPhoneSignUp(transaction: AuthPhoneTransaction, name: String): Result[Option[ApiAuth]] = {
    val phone = transaction.phoneNumber
    for {
      phoneAndCode ← fromOption(AuthRpcErrors.InvalidPhoneNumber)(normalizeWithCountry(phone).headOption)
      (_, countryCode) = phoneAndCode
      validName ← fromOption(CommonRpcErrors.InvalidName)(validName(name))
      user = User(
        id = nextIntId(),
        orgId = transaction.orgId,
        name = validName,
        createdAt = LocalDateTime.now(ZoneOffset.UTC))
      _ ← fromDBIO(UserRepo.create(user))
      userAuth = UserAuth(
        orgId = transaction.orgId,
        userId = user.id,
        phoneNumber = Some(phone),
        countryCode = Some(countryCode))
      _ ← fromDBIO(UserAuthRepo.create(userAuth))
      optApiAuth ← getOptApiAuth(transaction, Some(userAuth))
    } yield optApiAuth
  }

}
