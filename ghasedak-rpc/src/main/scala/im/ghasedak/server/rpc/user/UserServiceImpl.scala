package im.ghasedak.server.rpc.user

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import im.ghasedak.rpc.user.{ RequestLoadUsers, ResponseLoadUsers, UserService }
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.rpc.RpcError
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.user.UserExtension
import im.ghasedak.server.utils.concurrent.FutureResult
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UserServiceImpl(implicit system: ActorSystem) extends UserService
  with AuthTokenHelper
  with FutureResult[RpcError] {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)

  implicit private def onFailure: PartialFunction[Throwable, RpcError] = {
    case rpcError: RpcError ⇒ rpcError
    case ex ⇒
      log.error(ex, "Internal error")
      CommonRpcErrors.InternalError
  }

  override def loadUsers(request: RequestLoadUsers): Future[ResponseLoadUsers] = {
    authorize { clientData ⇒
      val action: Result[ResponseLoadUsers] = for {
        // todo: config
        _ ← fromBoolean(CommonRpcErrors.CollectionSizeLimitExceed)(request.userIds.size <= 100)
        users ← fromFuture(userExt.getUsers(clientData.orgId, clientData.userId, request.userIds))
      } yield ResponseLoadUsers(users)
      action.value
    }
  }

}
