package ir.sndu.server.rpc.auth.helper

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }

import ir.sndu.persist.repo.auth.AuthPhoneTransactionRepo
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.model.auth.AuthPhoneTransaction
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
            fromDBIO(AuthPhoneTransactionRepo.update(transaction.transactionHash, now))
          newTransaction = if (until > 15) None else Some(transaction.copy(createdAt = now))
        } yield newTransaction
      case None ⇒ point(optAuthTransaction)
    }
  }

  // todo: send sms code here
  protected def sendSmsCode(phoneNumber: Long, code: String): Result[Unit] = {
    fromFuture(Future.successful())
  }

}
