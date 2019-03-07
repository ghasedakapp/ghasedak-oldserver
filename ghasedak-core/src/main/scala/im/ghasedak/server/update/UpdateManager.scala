package im.ghasedak.server.update

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import com.sksamuel.pulsar4s.{ PulsarClient, PulsarClientConfig }
import com.typesafe.config.Config
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.update.UpdateEnvelope.Deliver
import org.apache.pulsar.client.api.Schema
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext
import scala.util.Random

case object StopOffice extends UpdatePayload

object UpdateManager {
  import im.ghasedak.server.serializer.ImplicitActorRef._

  val ShardingTypeName = EntityTypeKey[UpdatePayload]("UpdateManager")

  def shardingBehavior(entityId: String): Behavior[UpdatePayload] =
    Behaviors.receive { (ctx, msg) ⇒
      implicit val system: ActorSystem = ctx.system.toUntyped

      val config: Config = system.settings.config
      val pulsarHost: String = config.getString("module.update.pulsar.host")
      val pulsarPort: Int = config.getInt("module.update.pulsar.port")
      val pulsarClientConfig: PulsarClientConfig = PulsarClientConfig(s"pulsar://$pulsarHost:$pulsarPort")
      val pulsarClient = PulsarClient(pulsarClientConfig)

      implicit val updateMappingSchema: Schema[UpdateMapping] = UpdateMappingSchema()

      implicit val ec: ExecutionContext = system.dispatcher
      implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

      val log = Logging(system, getClass)

      val (userId, tokenId) = entityId.split("_").toList match {
        case a :: s :: Nil ⇒ (a.toInt, s.toLong)
        case _ ⇒
          val e = new RuntimeException("Wrong actor name")
          log.error(e, e.getMessage)
          throw e
      }

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

}
