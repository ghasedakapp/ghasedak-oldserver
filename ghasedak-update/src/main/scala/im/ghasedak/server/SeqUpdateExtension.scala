package im.ghasedak.server

import akka.actor._
import com.google.protobuf.ByteString
import com.sksamuel.pulsar4s._
import com.typesafe.config.Config
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.server.update.UpdateMapping
import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.client.impl.MessageIdImpl

import scala.concurrent.{ ExecutionContext, Future }

final class SeqUpdateExtensionImpl(system: ActorSystem) extends Extension
  with DeliveryOperations
  with DifferenceOperation {

  protected implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for updates
  protected implicit val ec: ExecutionContext = system.dispatcher

  private val config: Config = system.settings.config

  private val pulsarHost: String = config.getString("module.update.pulsar.host")
  private val pulsarPort: Int = config.getInt("module.update.pulsar.port")

  private val pulsarClientConfig: PulsarClientConfig =
    PulsarClientConfig(s"pulsar://$pulsarHost:$pulsarPort")

  protected val pulsarClient: PulsarClient =
    PulsarClient(pulsarClientConfig)

  protected implicit val updateMappingSchema: Schema[UpdateMapping] = UpdateMappingSchema()

  def getUserUpdateTopic(userId: Int): Topic = Topic(s"user_update_$userId")
  def getRoomUpdateTopic(roomId: Long): Topic = Topic(s"room_update_$roomId")

  def getApiSeqState(messageId: MessageId): ApiSeqState = {
    val state = MessageIdImpl.fromByteArray(messageId.bytes).asInstanceOf[MessageIdImpl]
    val seq = state.getEntryId
    ApiSeqState(seq.toInt, ByteString.copyFrom(state.toByteArray))
  }

  def getMessageId(seqState: ApiSeqState): MessageId = {
    MessageId.fromJava(MessageIdImpl.fromByteArray(seqState.state.toByteArray))
  }

  def getRoomState(roomId:Long): Future[ApiSeqState] =
    getState(getRoomUpdateTopic(roomId))

  def getUserState(userId:Int): Future[ApiSeqState] =
    getState(getUserUpdateTopic(userId))

  // fixme: find better way for get pulsar state
  def getState(topic: Topic): Future[ApiSeqState] = {
    val readerConfig = ReaderConfig(topic, MessageId.earliest)
    val reader = pulsarClient.reader[UpdateMapping](readerConfig)

    def getPulsarState(messageId: MessageId = MessageId.earliest): Future[MessageId] = {
      if (reader.hasMessageAvailable) {
        reader.nextAsync.flatMap(m ⇒ getPulsarState(m.messageId))
      } else {
        Future.successful(messageId)
      }
    }

    getPulsarState() map { messageId ⇒
      reader.closeAsync
      getApiSeqState(messageId)
    }
  }

}

object SeqUpdateExtension extends ExtensionId[SeqUpdateExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): SeqUpdateExtensionImpl = new SeqUpdateExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = SeqUpdateExtension
}
