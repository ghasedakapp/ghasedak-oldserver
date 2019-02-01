package im.ghasedak.server

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams.{ Control, source }
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait DifferenceOperation {
  this: SeqUpdateExtensionImpl ⇒

  def getSubscription(userId: Int, tokenId: String): Subscription = Subscription(s"${userId}_$tokenId")

  def getBaseUserUpdateConsumerConfig: ConsumerConfig =
    ConsumerConfig(Subscription.generate)

  def getConsumer(userId: Int, tokenId: String): Consumer[UpdateMapping] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = ConsumerConfig(
      subscriptionName = getSubscription(userId, tokenId),
      topics = Seq(topic))
    pulsarClient.consumer[UpdateMapping](consumerConfig)
  }

  // todo: config message retention and ttl on pulsar
  def getDifference(userId: Int, messageId: MessageId): Source[ConsumerMessage[UpdateMapping], Control] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)
    source(() ⇒ consumer, Some(messageId))
  }

  // todo: config message retention and ttl on pulsar
  def getDifference(consumer: Consumer[UpdateMapping]): Future[ConsumerMessage[UpdateMapping]] = {
    consumer.receiveAsync
  }

  // todo: config message retention and ttl on pulsar
  def acknowledge(consumer: Consumer[UpdateMapping], messageId: MessageId): Future[Unit] = {
    consumer.acknowledgeCumulativeAsync(messageId)
  }

}
