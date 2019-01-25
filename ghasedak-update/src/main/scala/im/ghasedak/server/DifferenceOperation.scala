package im.ghasedak.server

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams.{ Control, source }
import im.ghasedak.server.update.UpdateMapping

trait DifferenceOperation {
  this: SeqUpdateExtensionImpl ⇒

  def getBaseUserUpdateConsumerConfig: ConsumerConfig =
    ConsumerConfig(Subscription.generate)

  // todo: config message retention and ttl on pulsar
  def getDifference(userId: Int, messageId: MessageId): Source[ConsumerMessage[UpdateMapping], Control] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)
    source(() ⇒ consumer, Some(messageId))
  }

}
