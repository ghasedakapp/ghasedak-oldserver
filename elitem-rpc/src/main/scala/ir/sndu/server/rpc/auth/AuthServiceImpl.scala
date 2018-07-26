package ir.sndu.server.rpc.auth

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.auth.AuthServiceGrpc.AuthService
import ir.sndu.server.auth._

import scala.concurrent.Future

class AuthServiceImpl(implicit system: ActorSystem) extends AuthService with UserHandler {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)
  override def startPhoneAuth(request: RequestStartPhoneAuth): Future[ResponseStartPhoneAuth] = {
    Future.successful(ResponseStartPhoneAuth())
  }

  override def signUp(request: RequestSignUp): Future[ResponseAuth] = {
    val action = UserRepo.findByPhone(request.number) flatMap {
      case Some(u) ⇒
        createToken(u.id) map (ResponseAuth(_, Some(u.toApi())))

      case None ⇒
        for {
          user ← createUser(request.number, request.name, request.sex)
          token ← createToken(user.id)
        } yield ResponseAuth(token, Some(user))
    }
    db.run(action)
  }
}
