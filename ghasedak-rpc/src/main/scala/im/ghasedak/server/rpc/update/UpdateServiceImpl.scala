package im.ghasedak.server.rpc.update

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import im.ghasedak.rpc.update.UpdateServiceGrpc.UpdateService
import im.ghasedak.rpc.update.{ RequestGetDifference, RequestGetState, ResponseGetDifference, ResponseGetState }
import im.ghasedak.server.SeqUpdateExtension
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.rpc.RpcError
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.utils.concurrent.FutureResult
import io.grpc.stub.StreamObserver
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UpdateServiceImpl(implicit system: ActorSystem) extends UpdateService
  with AuthTokenHelper
  with UpdateServiceHelper
  with FutureResult[RpcError] {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val seqUpdateExt = SeqUpdateExtension(system)

  implicit private def onFailure: PartialFunction[Throwable, RpcError] = {
    case rpcError: RpcError ⇒ rpcError
    case ex ⇒
      log.error(ex, "Internal error")
      CommonRpcErrors.InternalError
  }

  override def getState(request: RequestGetState): Future[ResponseGetState] = {
    authorize { clientData ⇒
      val action: Result[ResponseGetState] = for {
        seqState ← fromFuture(seqUpdateExt.getState(clientData.userId))
      } yield ResponseGetState(Some(seqState))
      action.value
    }
  }

  override def getDifference(request: RequestGetDifference, responseObserver: StreamObserver[ResponseGetDifference]): Unit = {
    authorize { clientData ⇒
      (for {
        seqState ← fromOption(UpdateRpcErrors.SeqStateNotFound)(request.seqState)
      } yield {
        getDifference(clientData.userId, clientData.tokenId, seqState, responseObserver)
      }).value
    }
  }

}
