package im.ghasedak.server.rpc.update

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import akka.stream.scaladsl.Source
import im.ghasedak.rpc.update._
import im.ghasedak.server.SeqUpdateExtension
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.{ RpcError, RpcErrorHandler }
import im.ghasedak.server.utils.concurrent.FutureResult
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UpdateServiceImpl(implicit system: ActorSystem) extends UpdateServicePowerApi
  with AuthTokenHelper
  with UpdateServiceHelper
  with FutureResult[RpcError]
  with RpcErrorHandler {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val seqUpdateExt = SeqUpdateExtension(system)

  override def getState(request: RequestGetState, metadata: Metadata): Future[ResponseGetState] = {
    authorize(metadata) { clientData ⇒
      seqUpdateExt.getState(clientData.userId)
        .map(seqState ⇒ ResponseGetState(Some(seqState)))
    }
  }

  override def getDifference(request: RequestGetDifference, metadata: Metadata): Source[ResponseGetDifference, NotUsed] = {
    Source.fromFutureSource {
      authorize(metadata) { clientData ⇒
        val difference = fromOption(UpdateRpcErrors.SeqStateNotFound)(request.seqState) map (seqState ⇒ {
          getDifference(clientData.userId, clientData.tokenId, seqState)
        })
        difference.value
      }
    }
      .mapMaterializedValue(_ ⇒ NotUsed)
  }

  override def streamGetDifference(in: Source[StreamRequestGetDifference, NotUsed], metadata: Metadata): Source[StreamResponseGetDifference, NotUsed] =
    authorizeS(metadata) { clientData ⇒
      in.mapAsync(1) { req ⇒
        val result = if (req.ackState.isEmpty)
          receiveAsync(clientData.userId, clientData.tokenId)
        else
          ack(clientData.userId, req.ackState)
            .flatMap(_ ⇒ receiveAsync(clientData.userId, clientData.tokenId))
        result
      }
    }
}
