package ir.sndu.server.rpc.auth.helper

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }

import ir.sndu.api.auth.ApiAuth
import ir.sndu.api.user.ApiUser
import ir.sndu.persist.repo.auth.{ AuthPhoneTransactionRepo, AuthSessionRepo }
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.model.auth.{ AuthPhoneTransaction, AuthSession }
import ir.sndu.server.model.user.UserPhone
import ir.sndu.server.rpc.auth.{ AuthRpcErrors, AuthServiceImpl }

import scala.concurrent.Future

trait AuthServiceHelper {
  this: AuthServiceImpl ⇒

  protected def forbidDeletedUser(userId: Int): Result[Unit] =
    fromDBIOBoolean(AuthRpcErrors.UserIsDeleted)(UserRepo.isDeleted(userId).map(!_))

  protected def getOptAuthTransactionWithExpire(optAuthTransaction: Option[AuthPhoneTransaction]): Result[Option[AuthPhoneTransaction]] = {
    optAuthTransaction match {
      case Some(transaction) ⇒
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val until = now.until(transaction.createdAt, ChronoUnit.MINUTES)
        for {
          _ ← if (until > 15)
            fromDBIO(AuthPhoneTransactionRepo.delete(transaction.transactionHash))
          else
            fromDBIO(AuthPhoneTransactionRepo.updateCreateAt(transaction.transactionHash, now))
          newTransaction = if (until > 15) None else Some(transaction.copy(createdAt = now))
        } yield newTransaction
      case None ⇒ point(optAuthTransaction)
    }
  }

  // todo: send sms code here
  protected def sendSmsCode(phoneNumber: Long, code: String): Result[Unit] = {
    fromFuture(Future.successful())
  }

  protected def validatePhoneCode(transaction: AuthPhoneTransaction, code: String): Result[Unit] = {
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val until = now.until(transaction.createdAt, ChronoUnit.MINUTES)
    for {
      _ ← fromBoolean(AuthRpcErrors.InvalidAuthCode)(transaction.codeHash == code)
      _ ← fromBoolean(AuthRpcErrors.PhoneCodeExpired)(until < 15)
      _ ← fromDBIO(AuthPhoneTransactionRepo.updateIsChecked(transaction.transactionHash, isChecked = true))
    } yield ()
  }

  protected def getOptApiAuth(transaction: AuthPhoneTransaction, optUserPhone: Option[UserPhone]): Result[Option[ApiAuth]] = {
    optUserPhone match {
      case None ⇒ point(None)
      case Some(userPhone) ⇒
        for {
          // todo: fix this (delete account)
          user ← fromDBIO(UserRepo.find(userPhone.userId).head)
          generatedToken ← fromFuture(generateToken(user.id))
          (tokenId, token) = generatedToken
          authSession = AuthSession(
            user.id, tokenId, transaction.appId, transaction.apiKey,
            transaction.deviceHash, transaction.deviceInfo, LocalDateTime.now(ZoneOffset.UTC))
          _ ← fromDBIO(AuthSessionRepo.create(authSession))
          _ ← fromDBIO(AuthPhoneTransactionRepo.delete(transaction.transactionHash))
          apiUser = ApiUser(
            user.id, user.name, None, user.nickname, user.about, Some(userPhone.number))
        } yield Some(ApiAuth(token, Some(apiUser)))
    }
  }

}
