package ir.sndu.server.rpc.auth

import java.util.UUID

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.AuthIdRepo
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.auth.AuthServiceGrpc.AuthService
import ir.sndu.server.auth._
import slick.dbio._

import scala.concurrent.Future

class AuthServiceImpl(implicit system: ActorSystem) extends AuthService with UserHandler {
  import ir.sndu.server.rpc.ApiConversions._
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)
  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    println("salam")
    Future.successful(ResponseStartPhoneAuth())
  }

  override def signUp(request: RequestSignUp): Future[ResponseAuth] = {
    def createToken() =
      create(request.number, request.name, request.sex) map (u =>
        ResponseAuth(UUID.randomUUID().toString, Some(u)))

    val action = UserRepo.findByPhone(request.number) flatMap {
      case Some(u) =>
        AuthIdRepo.findByUserId(u.id).headOption flatMap {
          case Some(auth) =>
            DBIO.successful(ResponseAuth(auth.id, Some(
              ApiUser(
                u.id,
                u.name,
                u.nickname.getOrElse(""),
                u.sex,
                u.about.getOrElse("")))))
          case None => createToken()
        }

      case None => createToken()
    }
    db.run(action)
  }
}
