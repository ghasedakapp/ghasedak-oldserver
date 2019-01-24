package im.ghasedak.server.rpc.update

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.ConsumerMessage
import com.sksamuel.pulsar4s.akka.streams.Control
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.ResponseGetDifference
import im.ghasedak.server.update.UpdateMapping

trait UpdateServiceHelper {
  this: UpdateServiceImpl ⇒

  def getDifference(
    userId:   Int,
    tokenId:  String,
    seqState: ApiSeqState): Source[ResponseGetDifference, Control] = {
    val messageId = seqUpdateExt.getMessageId(seqState)
    seqUpdateExt.getDifference(userId, messageId)
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def buildDifference(tokenId: String, consumerMessage: ConsumerMessage[UpdateMapping]): ResponseGetDifference = {
    val seqState = seqUpdateExt.getApiSeqState(consumerMessage.messageId)
    val updateMapping = consumerMessage.value
    val updateContainer = if (updateMapping.custom.keySet.contains(tokenId))
      updateMapping.custom(tokenId)
    else
      updateMapping.default.get
    ResponseGetDifference(Some(seqState), Some(updateContainer))
  }

}
