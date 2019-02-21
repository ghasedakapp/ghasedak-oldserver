package im.ghasedak.server

import com.sksamuel.pulsar4s._
import im.ghasedak.api.update.{ ApiSeqState, ApiUpdateContainer }
import im.ghasedak.server.update.UpdateMapping

import scala.concurrent.Future

trait DeliveryOperations {
  this: SeqUpdateExtensionImpl ⇒

  def getBaseUserUpdateProducerConfig: ProducerConfig =
    ProducerConfig(Topic(""), enableBatching = Some(false))

  def deliverUserUpdate(
    userId:    Int,
    update:    ApiUpdateContainer,
    reduceKey: Option[String]     = None): Future[ApiSeqState] =
    deliverUpdate(
      getUserUpdateTopic(userId),
      UpdateMapping(default = Some(update)),
      reduceKey)

  def deliverRoomUpdate(
    roomId:    Long,
    update:    ApiUpdateContainer,
    reduceKey: Option[String]     = None): Future[ApiSeqState] =
    deliverUpdate(
      getRoomUpdateTopic(roomId),
      UpdateMapping(default = Some(update)),
      reduceKey)

  def deliverPeopleUpdate(
    userIds:   Seq[Int],
    update:    ApiUpdateContainer,
    reduceKey: Option[String]     = None): Future[Unit] =
    broadcastUpdate(
      userIds.map(getUserUpdateTopic),
      UpdateMapping(default = Some(update)),
      reduceKey)

  def deliverCustomUpdate(
    topic:   Topic,
    default: ApiUpdateContainer,
    custom:  Map[String, ApiUpdateContainer]): Future[ApiSeqState] =
    deliverUpdate(
      topic,
      UpdateMapping(
        default = Some(default),
        custom = custom))

  private def deliverUpdate(
    topic:     Topic,
    mapping:   UpdateMapping,
    reduceKey: Option[String] = None): Future[ApiSeqState] = {
    val producerConfig = getBaseUserUpdateProducerConfig.copy(topic = topic)
    val producer = pulsarClient.producer[UpdateMapping](producerConfig)
    val message = DefaultProducerMessage(reduceKey, mapping)
    producer.sendAsync(message) map { messageId ⇒
      producer.closeAsync
      getApiSeqState(messageId)
    }
  }

  private def broadcastUpdate(
    topics:    Seq[Topic],
    mapping:   UpdateMapping,
    reduceKey: Option[String] = None): Future[Unit] = {
    Future.sequence(topics map (topic ⇒ deliverUpdate(topic, mapping, reduceKey))) map (_ ⇒ ())
  }

}
