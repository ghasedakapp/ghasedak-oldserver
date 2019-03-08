package im.ghasedak.server.update

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }
import akka.actor.typed.{ Behavior, PostStop, Signal }
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import akka.stream.{ ActorMaterializer, SourceRef }
import akka.stream.scaladsl.StreamRefs
import com.typesafe.config.Config
import im.ghasedak.rpc.update.ResponseGetDifference
import im.ghasedak.server.Processor
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.serializer.ImplicitActorRef._
import im.ghasedak.server.update.UpdateEnvelope.{ StreamGetDifference, Subscribe }
import im.ghasedak.server.update.UpdateProcessor.StopOffice
import org.apache.pulsar.client.api.Schema
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

object UpdateProcessor {
  val ShardingTypeName: EntityTypeKey[UpdatePayload] = EntityTypeKey[UpdatePayload]("UpdateProcessor")

  case object StopOffice extends UpdatePayload

  def apply(entityId: String): Behavior[UpdatePayload] = {
    Behaviors.setup[UpdatePayload](context ⇒ new UpdateProcessor(context, entityId))
  }
}

class UpdateProcessor(context: ActorContext[UpdatePayload], entityId: String) extends Processor[UpdatePayload]
  with UpdateHelper
  with PulsarHelper {
  private implicit val system: ActorSystem = context.system.toUntyped
  private implicit val mat = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val db: PostgresProfile.backend.Database = DbExtension(system).db
  private val log = Logging(system, getClass)

  protected val config: Config = system.settings.config

  protected val (userId: Int, tokenId: String) = entityId.split("_").toList match {
    case a :: s :: Nil ⇒ (a.toInt, s)
    case _ ⇒
      val e = new RuntimeException("Wrong actor name")
      log.error(e, e.getMessage)

  }

  import im.ghasedak.server.serializer.ActorRefConversions._

  override def onReceive: Receive = {
    case s: StreamGetDifference ⇒
      val src = com.sksamuel.pulsar4s.akka.streams.source(() ⇒ createConsumer, None)
        .map(i ⇒ buildDifference(tokenId, i))

      val ref: Future[SourceRef[ResponseGetDifference]] = src.runWith(StreamRefs.sourceRef[ResponseGetDifference]())

      ref pipeTo s.replyTo
      Behaviors.same

    case s: Subscribe ⇒
      createConsumer

      s.replyTo ! Done
      Behaviors.same

    case StopOffice ⇒
      log.debug("Stopping ......")
      Behaviors.stopped
    case _ ⇒
      Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[UpdatePayload]] = {
    case PostStop ⇒
      context.log.info("UpdateProcessor  stopped")
      consumer.foreach(_.close())
      this
  }
}
