package im.ghasedak.server.rpc.auth.helper

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }

import com.auth0.jwt.JWT
import im.ghasedak.api.auth.ApiAuth
import im.ghasedak.api.user.ApiUser
import im.ghasedak.server.SeqUpdateExtension
import im.ghasedak.server.model.auth.{ AuthPhoneTransaction, AuthSession, AuthTransactionBase }
import im.ghasedak.server.model.org.ApiKey
import im.ghasedak.server.model.user.{ User, UserAuth }
import im.ghasedak.server.repo.auth.{ AuthSessionRepo, AuthTransactionRepo, GateAuthCodeRepo }
import im.ghasedak.server.repo.org.ApiKeyRepo
import im.ghasedak.server.repo.user.{ UserAuthRepo, UserRepo }
import im.ghasedak.server.rpc.auth.{ AuthRpcErrors, AuthServiceImpl }
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.user.UserUtils
import im.ghasedak.server.utils.CodeGen.genPhoneCode
import im.ghasedak.server.utils.StringUtils._
import im.ghasedak.server.utils.number.IdUtils._
import im.ghasedak.server.utils.number.PhoneNumberUtils._

import scala.concurrent.Future

trait AuthServiceHelper {
  this: AuthServiceImpl ⇒

  private val maximumAttempts: Int = 4 // attempts

  private val maximumValidCodeMinutes: Int = 15 // for 15 minutes phone code is valid

  private val seqExt = SeqUpdateExtension(system)

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

  // fixme: better implementation with adaptor method
  protected def sendSmsCode(phoneNumber: Long, transactionHash: String): Result[Unit] = {
    val codeHash = genPhoneCode(phoneNumber)
    for {
      _ ← fromDBIO(GateAuthCodeRepo.createOrUpdate(transactionHash, codeHash))
      // todo: send sms code here
    } yield ()
  }

  // fixme: better implementation with adaptor method
  protected def validateCode(transaction: AuthTransactionBase, code: String): Result[Unit] = {
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val until = transaction.createdAt.until(now, ChronoUnit.MINUTES)
    for {
      gateAuthCode ← fromDBIOOption(AuthRpcErrors.AuthCodeExpired)(GateAuthCodeRepo.find(transaction.transactionHash))
      attemptsIsOk ← if (gateAuthCode.attempts + 1 > maximumAttempts)
        for {
          _ ← fromDBIO(GateAuthCodeRepo.delete(transaction.transactionHash))
          _ ← fromDBIO(AuthTransactionRepo.delete(transaction.transactionHash))
        } yield false
      else
        fromDBIO(GateAuthCodeRepo.incrementAttempts(transaction.transactionHash, gateAuthCode.attempts)) map (_ ⇒ true)
      _ ← fromBoolean(AuthRpcErrors.AuthCodeExpired)(attemptsIsOk)
      _ ← fromBoolean(AuthRpcErrors.AuthCodeExpired)(until < maximumValidCodeMinutes)
      _ ← fromBoolean(AuthRpcErrors.InvalidAuthCode)(gateAuthCode.codeHash == code)
      _ ← fromDBIO(GateAuthCodeRepo.delete(transaction.transactionHash))
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

  protected def subscribe(optApiAuth: Option[ApiAuth]): Future[Unit] = {
    if (optApiAuth.isDefined) {
      val tokenId = JWT.decode(optApiAuth.get.token).getClaim("tokenId").asString()
      seqExt.subscribe(optApiAuth.get.user.get.id, tokenId)
    } else Future.successful()
  }

}
