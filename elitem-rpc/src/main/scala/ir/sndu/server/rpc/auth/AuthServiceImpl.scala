package ir.sndu.server.rpc.auth

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.UUID

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.DbExtension
import ir.sndu.persist.repo.auth.{ AuthPhoneTransactionRepo, AuthTransactionRepo }
import ir.sndu.persist.repo.user.UserPhoneRepo
import im.ghasedak.rpc.auth.AuthServiceGrpc.AuthService
import im.ghasedak.rpc.auth._
import ir.sndu.server.model.auth.{ AuthPhoneTransaction, AuthSession }
import ir.sndu.server.rpc.RpcError
import ir.sndu.server.rpc.auth.helper.{ AuthServiceHelper, AuthTokenHelper }
import ir.sndu.server.rpc.common.CommonRpcError
import ir.sndu.server.utils.concurrent.DBIOResult
import ir.sndu.server.utils.number.PhoneNumberUtils._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class AuthServiceImpl(implicit system: ActorSystem) extends AuthService
  with AuthServiceHelper
  with AuthTokenHelper
  with DBIOResult[RpcError] {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  implicit private def onFailure: PartialFunction[Throwable, RpcError] = {
    case rpcError: RpcError ⇒ rpcError
    case ex ⇒
      log.error(ex, "Internal error")
      CommonRpcError.InternalError
  }

  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    val action: Result[ResponseStartPhoneAuth] = for {
      _ ← fromBoolean(AuthRpcErrors.InvalidApiKey)(AuthSession.isValidApiKey(request.apiKey))
      normalizedPhone ← fromOption(AuthRpcErrors.InvalidPhoneNumber)(normalizeLong(request.phoneNumber).headOption)
      optUserPhone ← fromDBIO(UserPhoneRepo.findByPhoneNumber(normalizedPhone))
      // todo: fix this (delete account)
      _ ← optUserPhone map (p ⇒ forbidDeletedUser(p.userId)) getOrElse point(())
      optAuthTransaction ← fromDBIO(AuthPhoneTransactionRepo.findByPhoneAndDeviceHash(normalizedPhone, request.deviceHash))
      optAuthTransactionWithExpire ← getOptAuthTransactionWithExpire(optAuthTransaction)
      transactionHash ← optAuthTransactionWithExpire match {
        case Some(transaction) ⇒ point(transaction.transactionHash)
        case None ⇒
          val phoneAuthTransaction = AuthPhoneTransaction(
            phoneNumber = normalizedPhone,
            transactionHash = UUID.randomUUID().toString,
            appId = request.appId,
            apiKey = request.apiKey,
            deviceHash = request.deviceHash,
            deviceInfo = request.deviceInfo,
            createdAt = LocalDateTime.now(ZoneOffset.UTC))
          for {
            _ ← fromDBIO(AuthPhoneTransactionRepo.create(phoneAuthTransaction))
          } yield phoneAuthTransaction.transactionHash
      }
      _ ← sendSmsCode(normalizedPhone, transactionHash)
    } yield ResponseStartPhoneAuth(transactionHash)
    val result = db.run(action.value)
    result
  }

  override def validateCode(request: RequestValidateCode): Future[ResponseAuth] = {
    val action: Result[ResponseAuth] = for {
      transaction ← fromDBIOOption(AuthRpcErrors.AuthCodeExpired)(AuthTransactionRepo.findChildren(request.transactionHash))
      _ ← validateCode(transaction, request.code)
      optApiAuth ← transaction match {
        case apt: AuthPhoneTransaction ⇒
          for {
            // todo: fix this (delete account)
            optUserPhone ← fromDBIO(UserPhoneRepo.findByPhoneNumber(apt.phoneNumber))
            optApiAuth ← getOptApiAuth(apt, optUserPhone)
          } yield optApiAuth
      }
    } yield ResponseAuth(optApiAuth.isDefined, optApiAuth)
    val result = db.run(action.value)
    result
  }

  override def signUp(request: RequestSignUp): Future[ResponseAuth] = {
    val action: Result[ResponseAuth] = for {
      transaction ← fromDBIOOption(AuthRpcErrors.AuthCodeExpired)(AuthTransactionRepo.findChildren(request.transactionHash))
      _ ← fromBoolean(AuthRpcErrors.NotValidated)(transaction.isChecked)
      optApiAuth ← transaction match {
        case apt: AuthPhoneTransaction ⇒ newUserPhoneSignUp(apt, request.name)
      }
    } yield ResponseAuth(optApiAuth.isDefined, optApiAuth)
    val result = db.run(action.value)
    result
  }

}
