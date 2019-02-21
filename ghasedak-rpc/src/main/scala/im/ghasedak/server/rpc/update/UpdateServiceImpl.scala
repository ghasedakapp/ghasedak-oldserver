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
      //TODO topic validation
      if(request.roomIds.isEmpty){
        seqUpdateExt.getUserState(clientData.userId).map(state ⇒ Seq(ReceivedSeqState(clientData.userId.toLong, Some(state))))
      }else {
      Future.sequence(request.roomIds.map(t ⇒
        seqUpdateExt.getRoomState(t).map(state ⇒ ReceivedSeqState(t, Some(state)))))

      }
        .map(ResponseGetState(_))

    }
  }

  override def getDifference(request: RequestGetDifference, metadata: Metadata): Source[StreamingResponseGetDifference, NotUsed] = {
    authorizeFutureStream(metadata) { clientData ⇒
      fromOption(UpdateRpcErrors.SeqStateNotFound)(request.seqState) map (seqState ⇒ {
        getDifference(clientData.userId, clientData.tokenId, seqState)
      }) value
    }
  }

  override def streamingGetDifference(requestStream: Source[StreamingRequestGetDifference, NotUsed], metadata: Metadata): Source[StreamingResponseGetDifference, NotUsed] =
    //    authorizeStream(metadata) { clientData ⇒
    //      val consumer = seqUpdateExt.getConsumer(clientData.userId, clientData.tokenId)
    //      requestStream.flatMapConcat { req ⇒
    //        val result = acknowledge(consumer, req.seqStateAck).map { _ ⇒
    //          getDifference(clientData.userId, clientData.tokenId, req.seqStateAck.get).take(10)
    //        }
    //        Source.fromFutureSource(result)
    //      }
    //    }
    Source.empty

}
