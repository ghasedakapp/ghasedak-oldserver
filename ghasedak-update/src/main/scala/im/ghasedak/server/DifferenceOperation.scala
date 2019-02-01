package im.ghasedak.server

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams.{ CommittableMessage, Control, committableSource, source }
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait DifferenceOperation {
  this: SeqUpdateExtensionImpl ⇒

  def getBaseUserUpdateConsumerConfig: ConsumerConfig =
    ConsumerConfig(Subscription.generate)

  def getConsumer(userId: Int): Consumer[UpdateMapping] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    pulsarClient.consumer[UpdateMapping](consumerConfig)
  }

  // todo: config message retention and ttl on pulsar
  def getDifference(userId: Int, messageId: MessageId): Source[ConsumerMessage[UpdateMapping], Control] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)
    source(() ⇒ consumer, Some(messageId))
  }

  def streamGetDifference(userId: Int): Source[CommittableMessage[UpdateMapping], Control] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)
    committableSource(() ⇒ consumer)
  }

  def ack(userId: Int, messageId: MessageId): Future[Unit] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)
    consumer.acknowledgeCumulativeAsync(messageId)
  }

}
