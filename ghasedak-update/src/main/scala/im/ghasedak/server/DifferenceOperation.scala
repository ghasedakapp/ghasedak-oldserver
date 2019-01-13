package im.ghasedak.server

import com.sksamuel.pulsar4s._
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait DifferenceOperation {
  this: SeqUpdateExtensionImpl ⇒

  def getUserDifferenceReader(userId: Int, messageId: MessageId): Reader[UpdateMapping] = {
    val topic = getUserUpdateTopic(userId)
    val consumerConfig = ReaderConfig(topic, messageId)
    pulsarClient.reader[UpdateMapping](consumerConfig)
  }

  // todo: config message retention and ttl on pulsar
  def readNextDifference(reader: Reader[UpdateMapping]): Future[ConsumerMessage[UpdateMapping]] =
    reader.nextAsync

}
