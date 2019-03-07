package im.ghasedak.server.update

import akka.actor.typed.{ ActorRef, Props }
import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Scheduler }
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity, EntityRef }
import akka.util.Timeout
import com.google.protobuf.ByteString
import com.typesafe.config.Config
import im.ghasedak.api.update.ApiSeqState
import org.apache.pulsar.client.impl.MessageIdImpl

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.{ ClusterShardingSettings, ShardingEnvelope, ShardingMessageExtractor }
import com.sksamuel.pulsar4s.{ MessageId, Topic }
import akka.pattern.ask
import im.ghasedak.server.update.UpdateEnvelope.Deliver
import im.ghasedak.server.serializer.ActorRefConversions._

final class SeqUpdateExtensionImpl(system: ActorSystem) extends Extension
  with DeliveryOperations
  with DifferenceOperation {

  protected implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for updates
  protected implicit val ec: ExecutionContext = system.dispatcher

  protected implicit val timeout: Timeout = 15.seconds
  protected implicit val scheduler: Scheduler = system.scheduler

  private val sharding = ClusterSharding(system.toTyped)

  private val shardRegion: ActorRef[UpdateEnvelope] = sharding.init(Entity(
    typeKey = UpdateManager.ShardingTypeName,
    createBehavior = ctx ⇒ UpdateManager.shardingBehavior(ctx.entityId)).withStopMessage(StopOffice)
    .withMessageExtractor[UpdateEnvelope](new ShardingMessageExtractor[UpdateEnvelope, UpdatePayload] {
      override def entityId(message: UpdateEnvelope): String = s"${message.userId}_${message.tokenId}"

      override def shardId(entityId: String): String = (entityId.split("_").head.toInt % 10).toString

      override def unwrapMessage(message: UpdateEnvelope): UpdatePayload = message.payload.value.asInstanceOf[UpdatePayload]
    }))

  private def entity(userId: Int, tokenId: Long): EntityRef[UpdatePayload] = {
    sharding.entityRefFor(UpdateManager.ShardingTypeName, s"${userId}_${tokenId}")
  }

  private def construct(r: ActorRef[String]): Deliver = Deliver(replyTo = r)

  def deliver: Future[String] = {
    entity(10, 1000) ? construct
  }

  def getUserUpdateTopic(userId: Int): Topic = Topic(s"user_update_$userId")

  def getApiSeqState(messageId: MessageId): ApiSeqState = {
    val state = MessageIdImpl.fromByteArray(messageId.bytes).asInstanceOf[MessageIdImpl]
    val seq = state.getEntryId
    ApiSeqState(seq.toInt, ByteString.copyFrom(state.toByteArray))
  }

  def getMessageId(seqState: ApiSeqState): MessageId = {
    MessageId.fromJava(MessageIdImpl.fromByteArray(seqState.state.toByteArray))
  }

}

object SeqUpdateExtension extends ExtensionId[SeqUpdateExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): SeqUpdateExtensionImpl = new SeqUpdateExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = SeqUpdateExtension
}
