package im.ghasedak.server.rpc.update

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.rpc.update._
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.{ RpcError, RpcErrorHandler }
import im.ghasedak.server.update.SeqUpdateExtension
import im.ghasedak.server.utils.concurrent.FutureResult
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UpdateServiceImpl(implicit system: ActorSystem) extends UpdateServicePowerApi
  with AuthTokenHelper
  with UpdateServiceHelper
  with FutureResult[RpcError]
  with RpcErrorHandler {

  implicit protected val mat: ActorMaterializer = ActorMaterializer()

  protected val seqUpdateExt = SeqUpdateExtension(system)

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  override def getDifference(request: RequestGetDifference, metadata: Metadata): Future[ResponseGetDifference] =
    authorize(metadata) { clientData ⇒
      getDifference(clientData.userId, clientData.tokenId)
    }

  override def streamingGetDifference(requestStream: Source[StreamingRequestGetDifference, NotUsed], metadata: Metadata): Source[StreamingResponseGetDifference, NotUsed] =
    authorizeStream(metadata) { clientData ⇒
      acknowledge(requestStream, clientData.userId, clientData.tokenId)
      streamGetDifference(clientData.userId, clientData.tokenId)
    }

  override def acknowledge(request: RequestAcknowledge, metadata: Metadata): Future[ResponseVoid] =
    authorize(metadata) { clientData ⇒
      acknowledge(clientData.userId, clientData.tokenId, request.ackId) map (_ ⇒ ResponseVoid())
    }

  override def seek(request: SeekRequest, metadata: Metadata): Future[ResponseVoid] =
    authorize(metadata) { clientData ⇒
      val result = for {
        id ← fromOption(UpdateRpcErrors.SeqStateNotFound)(request.messageId)
        r ← fromFuture(seek(clientData.userId, clientData.tokenId, id) map (_ ⇒ ResponseVoid()))
      } yield r
      result.value
    }
}
