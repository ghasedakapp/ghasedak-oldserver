package ir.sndu.server.rpc.user

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import im.ghasedak.api.user.ApiUser
import im.ghasedak.rpc.user.UserServiceGrpc.UserService
import im.ghasedak.rpc.user.{ RequestLoadUsers, ResponseLoadUsers }
import ir.sndu.persist.db.DbExtension
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.rpc.RpcError
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper
import ir.sndu.server.user.UserExtension
import ir.sndu.server.utils.concurrent.DBIOResult
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UserServiceImpl(implicit system: ActorSystem) extends UserService
  with AuthTokenHelper
  with DBIOResult[RpcError] {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)

  override def loadUsers(request: RequestLoadUsers): Future[ResponseLoadUsers] =
    authorize { clientData ⇒
      val action =
        UserRepo.findUserContact(clientData.userId, request.userIds) map (_.map {
          case (user, contact) ⇒
            ApiUser(
              id = user.id,
              name = user.name,
              localName = contact.localName,
              Seq.empty,
              nickname = user.nickname,
              about = user.about)
        })

      if (request.userIds.size > 100)
        Future.failed(UserRpcErrors.LoadUserLimit)
      else
        db.run(action) map (ResponseLoadUsers(_))
    }
}
