package im.ghasedak.server.update

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }
import akka.actor.typed.{ Behavior, PostStop, Signal }
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import com.sksamuel.pulsar4s.{ PulsarClient, PulsarClientConfig }
import com.typesafe.config.Config
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.serializer.ImplicitActorRef._
import im.ghasedak.server.update.UpdateEnvelope.Deliver
import im.ghasedak.server.update.UpdateProcessor.StopOffice
import org.apache.pulsar.client.api.Schema
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext

object UpdateProcessor {
  val ShardingTypeName: EntityTypeKey[UpdatePayload] = EntityTypeKey[UpdatePayload]("UpdateProcessor")

  case object StopOffice extends UpdatePayload

  def apply(entityId: String): Behavior[UpdatePayload] = {
    Behaviors.setup[UpdatePayload](context ⇒ new UpdateProcessor(context, entityId))
  }
}

class UpdateProcessor(context: ActorContext[UpdatePayload], entityId: String) extends AbstractBehavior[UpdatePayload] {
  private implicit val system: ActorSystem = context.system.toUntyped
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val db: PostgresProfile.backend.Database = DbExtension(system).db
  private val log = Logging(system, getClass)

  private val config: Config = system.settings.config
  private val pulsarHost: String = config.getString("module.update.pulsar.host")
  private val pulsarPort: Int = config.getInt("module.update.pulsar.port")
  private val pulsarClientConfig: PulsarClientConfig = PulsarClientConfig(s"pulsar://$pulsarHost:$pulsarPort")
  private val pulsarClient = PulsarClient(pulsarClientConfig)

  private implicit val updateMappingSchema: Schema[UpdateMapping] = UpdateMappingSchema()

  val (userId, tokenId) = entityId.split("_").toList match {
    case a :: s :: Nil ⇒ (a.toInt, s.toLong)
    case _ ⇒
      val e = new RuntimeException("Wrong actor name")
      log.error(e, e.getMessage)
      throw e
  }

  override def onMessage(msg: UpdatePayload): Behavior[UpdatePayload] = {
    msg match {
      case d: Deliver ⇒
        d.replyTo ! "ack"
        Behaviors.same

      case StopOffice ⇒
        log.debug("Stopping ......")
        Behaviors.stopped
      case _ ⇒
        Behaviors.unhandled
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[UpdatePayload]] = {
    case PostStop ⇒
      context.log.info("UpdateProcessor  stopped")
      this
  }
}
