package im.ghasedak.server.update

import com.google.protobuf.ByteString
import com.sksamuel.pulsar4s._
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.{ ApiReceivedUpdate, ResponseGetDifference }
import org.apache.pulsar.client.impl.MessageIdImpl

trait UpdateHelper {
  def getUserUpdateTopic(userId: Int): Topic = Topic(s"user_update_$userId")

  def getApiSeqState(messageId: MessageId): ApiSeqState = {
    val state = MessageIdImpl.fromByteArray(messageId.bytes).asInstanceOf[MessageIdImpl]
    val seq = state.getEntryId
    ApiSeqState(seq.toInt, ByteString.copyFrom(state.toByteArray))
  }

  def getMessageId(seqState: ApiSeqState): MessageId = {
    MessageId.fromJava(MessageIdImpl.fromByteArray(seqState.state.toByteArray))
  }

  def getSubscription(userId: Int, tokenId: String): Subscription = Subscription(s"${userId}_$tokenId")

  def getBaseUserUpdateConsumerConfig: ConsumerConfig =
    ConsumerConfig(Subscription.generate)

  def buildDifference(tokenId: String, consumerMessage: ConsumerMessage[UpdateMapping]): ResponseGetDifference = {
    val seqState = getApiSeqState(consumerMessage.messageId)
    val updateMapping = consumerMessage.value
    val updateContainer = if (updateMapping.custom.keySet.contains(tokenId))
      updateMapping.custom(tokenId)
    else
      updateMapping.default.get
    ResponseGetDifference(Seq(ApiReceivedUpdate(Some(seqState), Some(updateContainer))))
  }
}
