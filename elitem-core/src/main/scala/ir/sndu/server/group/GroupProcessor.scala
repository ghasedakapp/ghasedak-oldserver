package ir.sndu.server.group

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import ir.sndu.server.GroupCommand
import ir.sndu.server.GroupCommands.Create

case object StopOffice extends GroupCommand

object GroupProcessor {

  val ShardingTypeName = EntityTypeKey[GroupCommand]("GroupProcessor")
  val MaxNumberOfShards = 1000

  def shardingBehavior(entityId: String): Behavior[GroupCommand] =
    Behaviors.receive { (ctx, msg) ⇒
      implicit val system = ctx.system.toUntyped
      val log = Logging(system, getClass)
      msg match {
        case c: Create ⇒

          Behaviors.same
        case StopOffice ⇒
          log.debug("Stopping ......")
          Behaviors.stopped
        case _ ⇒
          Behaviors.unhandled
      }
    }

}
