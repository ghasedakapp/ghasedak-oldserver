package im.ghasedak.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s._
import com.sksamuel.pulsar4s.akka.streams.source
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait DifferenceOperation {
  this: SeqUpdateExtensionImpl ⇒

  def getBaseUserUpdateConsumerConfig: ConsumerConfig =
    ConsumerConfig(Subscription.generate)

  // todo: config message retention and ttl on pulsar
  def getDifference(userId: Int, messageId: MessageId): Source[ConsumerMessage[UpdateMapping], NotUsed] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = getBaseUserUpdateConsumerConfig.copy(topics = Seq(topic))
    val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)
    source(() ⇒ consumer, Some(messageId)).mapMaterializedValue(_ ⇒ NotUsed)
  }

  // todo: remove this after akka grpc
  def getUserDifferenceReader(userId: Int, messageId: MessageId): Reader[UpdateMapping] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = ReaderConfig(topic, messageId)
    pulsarClient.reader[UpdateMapping](consumerConfig)
  }

  // todo: remove this after akka grpc
  def readNextDifference(reader: Reader[UpdateMapping]): Future[ConsumerMessage[UpdateMapping]] =
    reader.nextAsync

}
