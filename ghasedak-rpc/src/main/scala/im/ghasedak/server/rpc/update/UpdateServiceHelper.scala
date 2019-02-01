package im.ghasedak.server.rpc.update

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.{ Consumer, ConsumerMessage }
import com.sksamuel.pulsar4s.akka.streams.Control
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.{ ResponseGetDifference, StreamResponseGetDifference }
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait UpdateServiceHelper {
  this: UpdateServiceImpl ⇒

  def receiveAsync(
    userId:  Int,
    tokenId: String): Future[StreamResponseGetDifference] =
    seqUpdateExt.getConsumer(userId).receiveAsync
      .map(cm ⇒ buildDifference(tokenId, cm))
      .map(rsp ⇒ StreamResponseGetDifference(rsp.seqState, rsp.updateContainer))

  def getDifference(
    userId:   Int,
    tokenId:  String,
    seqState: ApiSeqState): Source[ResponseGetDifference, Control] = {
    val messageId = seqUpdateExt.getMessageId(seqState)
    seqUpdateExt.getDifference(userId, messageId)
      .filter(cm ⇒ cm.messageId != messageId) // because of pulsar architecture
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def streamGetDifference(
    userId:  Int,
    tokenId: String): Source[StreamResponseGetDifference, Control] = {
    seqUpdateExt.streamGetDifference(userId)
      .map(cm ⇒ buildDifference(tokenId, cm.message))
      .map(rsp ⇒ StreamResponseGetDifference(rsp.seqState, rsp.updateContainer))
  }

  def ack(userId: Int, seqState: Option[ApiSeqState]): Future[Unit] = {
    seqState match {
      case Some(state) ⇒
        val messageId = seqUpdateExt.getMessageId(state)
        seqUpdateExt.ack(userId, messageId)
      case None ⇒ Future.successful()
    }

  }

  private def buildDifference(tokenId: String, consumerMessage: ConsumerMessage[UpdateMapping]): ResponseGetDifference = {
    val seqState = seqUpdateExt.getApiSeqState(consumerMessage.messageId)
    val updateMapping = consumerMessage.value
    val updateContainer = if (updateMapping.custom.keySet.contains(tokenId))
      updateMapping.custom(tokenId)
    else
      updateMapping.default.get
    ResponseGetDifference(Some(seqState), Some(updateContainer))
  }

}
