package im.ghasedak.server.update

import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams.{ Control, source }

import scala.concurrent.Future

trait DifferenceOperation {
  this: SeqUpdateExtensionImpl ⇒

  def getConsumer(userId: Int, tokenId: String): Consumer[UpdateMapping] = ???
  //  {
  //    val topic = getUserUpdateTopic(userId)
  //    val consumerConfig = ConsumerConfig(
  //      subscriptionName = getSubscription(userId, tokenId),
  //      topics = Seq(topic))
  //    pulsarClient.consumer[UpdateMapping](consumerConfig)
  //  }

  def generateConsumer(userId: Int): Consumer[UpdateMapping] = ???
  //  {
  //    val topic = getUserUpdateTopic(userId)
  //    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
  //    pulsarClient.consumer[UpdateMapping](consumerConfig)
  //  }

  // todo: config message retention and ttl on pulsar
  def getDifference(userId: Int, tokenId: String): Future[ConsumerMessage[UpdateMapping]] = {
    val consumer = getConsumer(userId, tokenId)
    consumer.receiveAsync
  }

  def acknowledge(userId: Int, tokenId: String, messageId: MessageId): Future[Unit] = {
    val consumer = getConsumer(userId, tokenId + "-ack")
    consumer.acknowledgeCumulativeAsync(messageId)
  }

  def seek(userId: Int, tokenId: String, messageId: MessageId): Future[Unit] = {
    val consumer = getConsumer(userId, tokenId)
    consumer.seekAsync(messageId)
  }

}
