package ir.sndu.server.rpc.auth

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.auth.AuthServiceGrpc.AuthService
import ir.sndu.server.auth._
import ir.sndu.server.rpc.auth.helper.AuthHelper
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

class AuthServiceImpl(implicit system: ActorSystem) extends AuthService
  with AuthHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = PostgresDb.db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    Future.successful(ResponseStartPhoneAuth())
  }

  override def signUp(request: RequestSignUp): Future[ResponseAuth] = {
    Future.successful(ResponseAuth())
  }
}
