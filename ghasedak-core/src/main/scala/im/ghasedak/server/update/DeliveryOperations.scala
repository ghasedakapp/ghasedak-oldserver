package im.ghasedak.server.update

import com.sksamuel.pulsar4s._
import im.ghasedak.api.update.{ ApiSeqState, ApiUpdateContainer }
import org.apache.pulsar.client.api.Schema

import scala.concurrent.Future

trait DeliveryOperations extends UpdateHelper {
  this: SeqUpdateExtensionImpl ⇒

  private val config = system.settings.config
  private val pulsarHost: String = config.getString("module.update.pulsar.host")
  private val pulsarPort: Int = config.getInt("module.update.pulsar.port")
  private val pulsarClientConfig: PulsarClientConfig = PulsarClientConfig(s"pulsar://$pulsarHost:$pulsarPort")
  private val pulsarClient = PulsarClient(pulsarClientConfig)

  private implicit val updateMappingSchema: Schema[UpdateMapping] = UpdateMappingSchema()

  def getBaseUserUpdateProducerConfig: ProducerConfig =
    ProducerConfig(Topic(""), enableBatching = Some(false))

  def deliverUserUpdate(
    userId:    Int,
    update:    ApiUpdateContainer,
    reduceKey: Option[String]     = None): Future[ApiSeqState] =
    deliverUpdate(
      userId,
      UpdateMapping(default = Some(update)),
      reduceKey)

  def deliverPeopleUpdate(
    userIds:   Seq[Int],
    update:    ApiUpdateContainer,
    reduceKey: Option[String]     = None): Future[Unit] =
    broadcastUpdate(
      userIds,
      UpdateMapping(default = Some(update)),
      reduceKey)

  def deliverCustomUpdate(
    userId:  Int,
    default: ApiUpdateContainer,
    custom:  Map[String, ApiUpdateContainer]): Future[ApiSeqState] =
    deliverUpdate(
      userId,
      UpdateMapping(
        default = Some(default),
        custom = custom))

  def broadcastUserUpdate(
    userId:       Int,
    bcastUserIds: Set[Int],
    update:       ApiUpdateContainer,
    reduceKey:    Option[String]     = None): Future[ApiSeqState] = {
    val mapping = UpdateMapping(default = Some(update))
    for {
      seqState ← deliverUpdate(userId, mapping, reduceKey)
      _ ← broadcastUpdate((bcastUserIds - userId).toSeq, mapping, reduceKey)
    } yield seqState
  }

  private def deliverUpdate(
    userId:    Int,
    mapping:   UpdateMapping,
    reduceKey: Option[String] = None): Future[ApiSeqState] = {
    Future.successful(ApiSeqState())
    val topic = getUserUpdateTopic(userId)
    val producerConfig = getBaseUserUpdateProducerConfig.copy(topic = topic)
    val producer = pulsarClient.producer[UpdateMapping](producerConfig)
    val message = DefaultProducerMessage(reduceKey, mapping)
    producer.sendAsync(message) map { messageId ⇒
      producer.closeAsync
      getApiSeqState(messageId)
    }
  }

  private def broadcastUpdate(
    userIds:   Seq[Int],
    mapping:   UpdateMapping,
    reduceKey: Option[String] = None): Future[Unit] = {
    Future.sequence(userIds map (userId ⇒ deliverUpdate(userId, mapping, reduceKey))) map (_ ⇒ ())
  }

}
