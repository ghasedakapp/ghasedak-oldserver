package im.ghasedak.server.rpc.update

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.ConsumerMessage
import com.sksamuel.pulsar4s.akka.streams.Control
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.{ ReceivedUpdate, ResponseGetDifference, StreamingRequestGetDifference, StreamingResponseGetDifference }
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait UpdateServiceHelper {
  this: UpdateServiceImpl ⇒

  def streamGetDifference(
    userId:  Int,
    tokenId: String): Source[StreamingResponseGetDifference, Control] = {
    seqUpdateExt.streamGetDifference(userId, tokenId)
      .map { cm ⇒
        StreamingResponseGetDifference(buildDifference(tokenId, cm).receivedUpdates)
      }
  }

  def getDifference(userId: Int, tokenId: String): Future[ResponseGetDifference] = {
    seqUpdateExt.getDifference(userId, tokenId)
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def acknowledge(userId: Int, tokenId: String, seqState: Option[ApiSeqState]): Future[Unit] = {
    seqState match {
      case Some(state) ⇒
        val messageId = seqUpdateExt.getMessageId(state)
        seqUpdateExt.acknowledge(userId, tokenId, messageId)
      case None ⇒ Future.successful()
    }
  }

  def acknowledge(
    requestStream: Source[StreamingRequestGetDifference, NotUsed],
    userId:        Int, tokenId: String): Unit =
    requestStream.runForeach(req ⇒ acknowledge(userId, tokenId, req.ackId))
      .onComplete {
        case Success(_) ⇒ log.info("Get diff request stream completed successfully")
        case Failure(e) ⇒ log.error(e, "Error in get diff request stream")
      }

  def seek(userId: Int, tokenId: String, state: ApiSeqState): Future[Unit] = {
    val messageId = seqUpdateExt.getMessageId(state)
    seqUpdateExt.seek(userId, tokenId, messageId)
  }

  private def buildDifference(tokenId: String, consumerMessage: ConsumerMessage[UpdateMapping]): ResponseGetDifference = {
    val seqState = seqUpdateExt.getApiSeqState(consumerMessage.messageId)
    val updateMapping = consumerMessage.value
    val updateContainer = if (updateMapping.custom.keySet.contains(tokenId))
      updateMapping.custom(tokenId)
    else
      updateMapping.default.get
    ResponseGetDifference(Seq(ReceivedUpdate(Some(seqState), Some(updateContainer))))
  }

}
