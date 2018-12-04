package ir.sndu.server.rpc.user

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.rpc.auth.helper.AuthHelper
import ir.sndu.rpc.user.UserServiceGrpc.UserService
import ir.sndu.rpc.user._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

class UsersServiceImpl(implicit system: ActorSystem) extends UserService
  with AuthHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = PostgresDb.db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  override def loadFullUsers(request: RequestLoadFullUsers): Future[ResponseLoadFullUsers] =
    authorize { _ ⇒
      db.run(UserRepo.findByIds(request.userPeers.map(_.userId).toSet)).map { users ⇒
        users map (_.toApi)
      } map (ResponseLoadFullUsers(_))
    }
}
