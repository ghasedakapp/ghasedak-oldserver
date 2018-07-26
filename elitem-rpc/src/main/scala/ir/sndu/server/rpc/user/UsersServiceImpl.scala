package ir.sndu.server.rpc.user

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.rpc.auth.AuthHelper
import ir.sndu.server.rpc.users.{ RequestLoadFullUsers, ResponseLoadFullUsers }
import ir.sndu.server.rpc.users.UserServiceGrpc.UserService

import scala.concurrent.Future

class UsersServiceImpl(implicit system: ActorSystem) extends UserService
  with AuthHelper {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)

  override def loadFullUsers(request: RequestLoadFullUsers): Future[ResponseLoadFullUsers] =
    authorize(request.token) { userId ⇒
      db.run(UserRepo.findByIds(request.userPeers.map(_.userId).toSet)).map { users ⇒
        users map (_.toApi())
      } map (ResponseLoadFullUsers(_))
    }
}
