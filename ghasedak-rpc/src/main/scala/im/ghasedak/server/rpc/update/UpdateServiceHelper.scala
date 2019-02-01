package im.ghasedak.server.rpc.update

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.akka.streams.Control
import com.sksamuel.pulsar4s.{ Consumer, ConsumerMessage }
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.StreamingResponseGetDifference
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait UpdateServiceHelper {
  this: UpdateServiceImpl ⇒

  def getDifference(
    userId:   Int,
    tokenId:  String,
    seqState: ApiSeqState): Source[StreamingResponseGetDifference, Control] = {
    val messageId = seqUpdateExt.getMessageId(seqState)
    seqUpdateExt.getDifference(userId, messageId)
      .filter(cm ⇒ cm.messageId != messageId) // because of pulsar architecture
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def getDifference(tokenId: String, consumer: Consumer[UpdateMapping]): Future[StreamingResponseGetDifference] = {
    seqUpdateExt.getDifference(consumer)
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def acknowledge(consumer: Consumer[UpdateMapping], seqState: Option[ApiSeqState]): Future[Unit] = {
    seqState match {
      case Some(state) ⇒
        val messageId = seqUpdateExt.getMessageId(state)
        seqUpdateExt.acknowledge(consumer, messageId)
      case None ⇒ Future.successful()
    }
  }

  private def buildDifference(tokenId: String, consumerMessage: ConsumerMessage[UpdateMapping]): StreamingResponseGetDifference = {
    val seqState = seqUpdateExt.getApiSeqState(consumerMessage.messageId)
    val updateMapping = consumerMessage.value
    val updateContainer = if (updateMapping.custom.keySet.contains(tokenId))
      updateMapping.custom(tokenId)
    else
      updateMapping.default.get
    StreamingResponseGetDifference(Some(seqState), Some(updateContainer))
  }

}
