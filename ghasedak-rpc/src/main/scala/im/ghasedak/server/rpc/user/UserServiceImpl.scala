package im.ghasedak.server.rpc.user

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import im.ghasedak.api.user.User
import im.ghasedak.rpc.user._
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.repo.contact.UserContactRepo
import im.ghasedak.server.repo.user.UserRepo
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.rpc.{ RpcError, RpcErrorHandler }
import im.ghasedak.server.user.UserExtension
import im.ghasedak.server.utils.concurrent.{ DBIOHelper, FutureResult }
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }
final class UserServiceImpl(implicit system: ActorSystem) extends UserServicePowerApi
  with AuthTokenHelper
  with FutureResult[RpcError]
  with RpcErrorHandler {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)

  override def loadUsers(request: RequestLoadUsers, metadata: Metadata): Future[ResponseLoadUsers] = {
    authorize(metadata) { clientData ⇒
      val action: Result[ResponseLoadUsers] = for {
        // todo: config
        _ ← fromBoolean(CommonRpcErrors.CollectionSizeLimitExceed)(request.userIds.size <= 20)
        users ← fromFuture(getUsers(clientData.orgId, clientData.userId, request.userIds.toSet))
      } yield ResponseLoadUsers(users.toSeq)
      action.value
    }
  }

  def getUsers(clientOrgId: Int, clientUserId: Int, userIds: Set[Int]): Future[Set[User]] =
    Future.sequence(userIds.map(uid ⇒ getUser(clientOrgId, clientUserId, uid))).map(_.flatten)

  def getUser(clientOrgId: Int, clientUserId: Int, userId: Int): Future[Option[User]] = {
    val action =
      for {
        userOpt ← UserRepo.find(clientOrgId, userId)
        localNameOpt ← DBIOHelper.fromOption(userOpt.map(u ⇒ UserContactRepo.findName(clientUserId, u.id)))
      } yield userOpt.map(_.toApi(localNameOpt.flatten))

    db.run(action)
  }

}
