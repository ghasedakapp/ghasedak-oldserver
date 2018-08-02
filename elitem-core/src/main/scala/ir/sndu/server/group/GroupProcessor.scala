package ir.sndu.server.group

import java.time.Instant

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import akka.persistence.typed.scaladsl.{ Effect, PersistentBehaviors }
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import ir.sndu.server.group.GroupCommands.{ Create, CreateAck }
import ir.sndu.server.group.GroupEvents.Created
import akka.actor.typed.scaladsl.adapter._

case object StopOffice extends GroupCommand

object GroupProcessor {
  import ir.sndu.server.ImplicitActorRef._

  val ShardingTypeName = EntityTypeKey[GroupCommand]("GroupProcessor")
  val MaxNumberOfShards = 1000

  def shardingBehavior(entityId: String): Behavior[GroupCommand] =
    PersistentBehaviors.receive[GroupCommand, GroupEvent, GroupState](
      persistenceId = ShardingTypeName.name + "-" + entityId,
      emptyState = GroupState.empty,
      commandHandler(entityId),
      eventHandler) onRecoveryCompleted recovery

  private def commandHandler(entityId: String): CommandHandler[GroupCommand, GroupEvent, GroupState] =
    CommandHandler.byState {
      case state ⇒ initial(entityId)
    }

  private def initial(entityId: String): CommandHandler[GroupCommand, GroupEvent, GroupState] =
    (ctx, state, cmd) ⇒ {
      implicit val system = ctx.system.toUntyped
      val log = Logging(system, getClass)
      cmd match {
        case c: Create ⇒
          log.debug("Persisting....")
          val evt = Created(Instant.now(), entityId.toInt)
          Effect.persist(evt).andThen { _ ⇒
            c.replyTo ! CreateAck
          }
        case StopOffice ⇒
          log.debug("Stopping ......")
          Effect.stop
        case _ ⇒
          Effect.unhandled
      }

    }

  private val eventHandler: (GroupState, GroupEvent) ⇒ GroupState = (state, event) ⇒ state.update(event)

  private val recovery: (ActorContext[GroupCommand], GroupState) ⇒ Unit = { (ctx, state) ⇒

  }

}
