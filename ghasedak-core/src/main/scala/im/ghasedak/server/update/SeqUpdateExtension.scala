package im.ghasedak.server.update

import akka.{ Done, NotUsed }
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Scheduler }
import akka.cluster.sharding.typed.ShardingMessageExtractor
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity, EntityRef }
import akka.stream.SourceRef
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.google.protobuf.ByteString
import com.sksamuel.pulsar4s.{ MessageId, Topic }
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.ResponseGetDifference
import im.ghasedak.server.serializer.ActorRefConversions._
import im.ghasedak.server.update.UpdateEnvelope.{ Acknowledge, StreamGetDifference, Subscribe }
import im.ghasedak.server.update.UpdateProcessor.StopOffice
import org.apache.pulsar.client.impl.MessageIdImpl

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

final class SeqUpdateExtensionImpl(val system: ActorSystem) extends Extension
  with DeliveryOperations
  with DifferenceOperation {

  protected implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for updates
  protected implicit val ec: ExecutionContext = system.dispatcher

  protected implicit val timeout: Timeout = 15.seconds
  protected implicit val scheduler: Scheduler = system.scheduler

  private val sharding = ClusterSharding(system.toTyped)

  private val shardRegion: ActorRef[UpdateEnvelope] = sharding.init(Entity(
    typeKey = UpdateProcessor.ShardingTypeName,
    createBehavior = ctx ⇒ UpdateProcessor.apply(ctx.entityId))
    .withStopMessage(StopOffice)
    .withMessageExtractor[UpdateEnvelope](new ShardingMessageExtractor[UpdateEnvelope, UpdatePayload] {
      override def entityId(message: UpdateEnvelope): String = s"${message.userId}_${message.tokenId}"

      override def shardId(entityId: String): String = (entityId.split("_").head.toInt % 10).toString

      override def unwrapMessage(message: UpdateEnvelope): UpdatePayload = message.payload.value.asInstanceOf[UpdatePayload]
    }))

  private def entity(userId: Int, tokenId: String): EntityRef[UpdatePayload] = {
    sharding.entityRefFor(UpdateProcessor.ShardingTypeName, s"${userId}_${tokenId}")
  }

  def stop(userId: Int, tokenId: String): Unit = {
    entity(userId, tokenId) ! StopOffice
  }

  private def streamAsk(r: ActorRef[SourceRef[ResponseGetDifference]]): StreamGetDifference = StreamGetDifference(replyTo = r)
  def streamGetDifference(userId: Int, tokenId: String): Source[ResponseGetDifference, NotUsed] = {
    val src = entity(userId, tokenId).ask[SourceRef[ResponseGetDifference]](f ⇒ streamAsk(f))
    Source.fromFuture(src.map(_.source)).flatMapConcat(identity)
  }

  private def subscribeAsk(r: ActorRef[Done]): Subscribe = Subscribe(replyTo = r)
  def subscribe(userId: Int, tokenId: String): Future[Unit] = {
    (entity(userId, tokenId) ? subscribeAsk) map (_ ⇒ ())
  }

  private def acknowledgeAsk(seqState: Option[ApiSeqState])(r: ActorRef[Done]): Acknowledge = Acknowledge(replyTo = r, seqState)
  def acknowledge(userId: Int, tokenId: String, seqState: Option[ApiSeqState]): Future[Unit] = {
    (entity(userId, tokenId) ? acknowledgeAsk(seqState)) map (_ ⇒ ())
  }

}

object SeqUpdateExtension extends ExtensionId[SeqUpdateExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): SeqUpdateExtensionImpl = new SeqUpdateExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = SeqUpdateExtension
}
