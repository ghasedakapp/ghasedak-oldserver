package im.ghasedak.server.rpc.auth

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.UUID

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import im.ghasedak.rpc.auth._
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.model.auth.AuthPhoneTransaction
import im.ghasedak.server.repo.auth._
import im.ghasedak.server.repo.user.UserAuthRepo
import im.ghasedak.server.rpc._
import im.ghasedak.server.rpc.auth.helper._
import im.ghasedak.server.utils.concurrent.DBIOResult
import im.ghasedak.server.utils.number.PhoneNumberUtils._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class AuthServiceImpl(implicit system: ActorSystem) extends AuthServicePowerApi
  with AuthServiceHelper
  with AuthTokenHelper
  with DBIOResult[RpcError]
  with RpcErrorHandler {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  override def startPhoneAuth(request: RequestStartPhoneAuth, metadata: Metadata): Future[ResponseStartPhoneAuth] = {
    val action: Result[ResponseStartPhoneAuth] = for {
      optApiKey ← getApiKey(request.apiKey)
      apiKey ← fromOption(AuthRpcErrors.InvalidApiKey)(optApiKey)
      normalizedPhone ← fromOption(AuthRpcErrors.InvalidPhoneNumber)(normalizeLong(request.phoneNumber).headOption)
      optUserAuth ← fromDBIO(UserAuthRepo.findByPhoneNumberAndOrgId(normalizedPhone, apiKey.orgId))
      // todo: fix this (delete account)
      _ ← optUserAuth map (ua ⇒ forbidDeletedUser(ua.userId)) getOrElse point(())
      optAuthTransaction ← fromDBIO(AuthPhoneTransactionRepo.findByPhoneNumberAndOrgId(normalizedPhone, apiKey.orgId))
      optAuthTransactionWithExpire ← getOptAuthTransactionWithExpire(optAuthTransaction)
      transactionHash ← optAuthTransactionWithExpire match {
        case Some(transaction) ⇒ point(transaction.transactionHash)
        case None ⇒
          val phoneAuthTransaction = AuthPhoneTransaction(
            phoneNumber = normalizedPhone,
            transactionHash = UUID.randomUUID().toString,
            orgId = apiKey.orgId,
            apiKey = apiKey.apiKey,
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

  override def validateCode(request: RequestValidateCode, metadata: Metadata): Future[ResponseAuth] = {
    val action: Result[ResponseAuth] = for {
      transaction ← fromDBIOOption(AuthRpcErrors.AuthCodeExpired)(AuthTransactionRepo.findChildren(request.transactionHash))
      _ ← validateCode(transaction, request.code)
      optApiAuth ← transaction match {
        case apt: AuthPhoneTransaction ⇒
          for {
            // todo: fix this (delete account)
            optUserAuth ← fromDBIO(UserAuthRepo.findByPhoneNumberAndOrgId(apt.phoneNumber, transaction.orgId))
            optApiAuth ← getOptApiAuth(apt, optUserAuth)
          } yield optApiAuth
      }
    } yield ResponseAuth(optApiAuth.isDefined, optApiAuth)
    val result = db.run(action.value)
    result
  }

  override def signUp(request: RequestSignUp, metadata: Metadata): Future[ResponseAuth] = {
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

  override def signOut(request: RequestSignOut, metadata: Metadata): Future[ResponseVoid] = {
    authorize(metadata) { clientData ⇒
      val action: Result[ResponseVoid] = for {
        _ ← fromDBIO(AuthTokenRepo.delete(clientData.tokenId))
        _ ← fromDBIO(AuthSessionRepo.delete(clientData.tokenId))
      } yield ResponseVoid()
      val result = db.run(action.value)
      result
    }
  }

}
