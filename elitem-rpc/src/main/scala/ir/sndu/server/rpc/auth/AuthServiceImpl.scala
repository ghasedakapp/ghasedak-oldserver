package ir.sndu.server.rpc.auth

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }
import java.util.UUID

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.auth.AuthPhoneTransactionRepo
import ir.sndu.persist.repo.user.{ UserPhoneRepo, UserRepo }
import ir.sndu.rpc.auth.AuthServiceGrpc.AuthService
import ir.sndu.rpc.auth._
import ir.sndu.server.model.auth.AuthPhoneTransaction
import ir.sndu.server.rpc.RpcError
import ir.sndu.server.rpc.auth.helper.AuthHelper
import ir.sndu.server.rpc.common.CommonRpcError
import ir.sndu.server.utils.concurrent.DBIOResult
import ir.sndu.server.utils.number.PhoneNumberUtils._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

class AuthServiceImpl(implicit system: ActorSystem) extends AuthService
  with AuthHelper with DBIOResult[RpcError] {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = PostgresDb.db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  implicit private def onFailure: PartialFunction[Throwable, RpcError] = {
    case _ ⇒ CommonRpcError.InternalError
  }

  protected def forbidDeletedUser(userId: Int): Result[Unit] =
    fromDBIOBoolean(AuthRpcErrors.UserIsDeleted)(UserRepo.isDeleted(userId).map(!_))

  private def getOptAuthTransactionWithExpire(optAuthTransaction: Option[AuthPhoneTransaction]): Result[Option[AuthPhoneTransaction]] = {
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

  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    val action = for {
      normalizedPhone ← fromOption(AuthRpcErrors.InvalidPhoneNumber)(normalizeLong(request.phoneNumber).headOption)
      optPhone ← fromDBIO(UserPhoneRepo.findByPhoneNumber(normalizedPhone).headOption)
      _ ← optPhone map (p ⇒ forbidDeletedUser(p.userId)) getOrElse point(())
      optAuthTransaction ← fromDBIO(AuthPhoneTransactionRepo.findByPhoneAndDeviceHash(normalizedPhone, request.deviceHash))
      optAuthTransactionWithExpire ← getOptAuthTransactionWithExpire(optAuthTransaction)
      transactionHash ← optAuthTransactionWithExpire match {
        case Some(transaction) ⇒ point(transaction.transactionHash)
        case None ⇒
          val phoneAuthTransaction = AuthPhoneTransaction(
            normalizedPhone,
            UUID.randomUUID().toString,
            request.appId,
            request.apiKey,
            request.deviceHash,
            request.deviceInfo,
            LocalDateTime.now(ZoneOffset.UTC))
          for {
            _ ← fromDBIO(AuthPhoneTransactionRepo.create(phoneAuthTransaction))
          } yield phoneAuthTransaction.transactionHash
      }
      // todo: send sms code here
      isRegistered = optPhone.isDefined
    } yield ResponseStartPhoneAuth(transactionHash, isRegistered)
    val result = db.run(action.value)
    result
  }

}
