package ir.sndu.server.rpc.auth

import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

import akka.actor.ActorSystem
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.{ UserPhoneRepo, UserRepo }
import ir.sndu.server.auth.AuthServiceGrpc.AuthService
import ir.sndu.server.auth._
import ir.sndu.server.model.user.{ User, UserState }
import slick.dbio._

import scala.concurrent.Future

class AuthServiceImpl(implicit system: ActorSystem) extends AuthService with UserHandler {
  import ir.sndu.server.rpc.ApiConversions._
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    println("salam")
    Future.successful(ResponseStartPhoneAuth())
  }

  override def signUp(request: RequestSignUp): Future[ResponseAuth] = {
    val action = UserRepo.findByPhone(request.number) flatMap {
      case Some(u) => DBIO.successful(ResponseAuth("", Some(ApiUser(u.id, u.name, u.nickname.getOrElse(""), u.sex, u.about.getOrElse("")))))
      case None => create(request.number, request.name, request.sex) map (u => ResponseAuth("", Some(u)))
    }
    db.run(action)
  }
}
