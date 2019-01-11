package im.ghasedak.server.rpc.user

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import im.ghasedak.rpc.user.UserServiceGrpc.UserService
import im.ghasedak.rpc.user.{ RequestLoadUsers, ResponseLoadUsers }
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.rpc.RpcError
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.user.UserExtension
import im.ghasedak.server.utils.concurrent.DBIOResult
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
      // todo: config
      if (request.userIds.size > 100)
        Future.failed(CommonRpcErrors.CollectionSizeLimitExceed)
      else
        userExt.getUsers(clientData.orgId, clientData.userId, request.userIds) map (ResponseLoadUsers(_))
    }

}
