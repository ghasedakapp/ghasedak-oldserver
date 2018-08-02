package ir.sndu.server.group

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import ir.sndu.server.group.GroupCommands.{ Create, CreateAck }

case object StopOffice extends GroupCommand

object GroupProcessor {
  import ir.sndu.server.ImplicitActorRef._

  val ShardingTypeName = EntityTypeKey[GroupCommand]("GroupProcessor")
  val MaxNumberOfShards = 1000

  val behavior: Behavior[GroupCommand] = commandHandler

  private def commandHandler: Behavior[GroupCommand] = {
    Behaviors.receive { (ctx, msg) ⇒
      implicit val system = ctx.system.toUntyped
      val log = Logging(system, getClass)
      msg match {
        case c: Create ⇒
          log.debug("Receive ====> {}", c)
          log.debug("Actor name : {}", ctx.self.path.name)
          c.replyTo ! CreateAck()
          Behaviors.same
        case StopOffice ⇒
          log.debug("Stopping ......")
          Behaviors.same

      }
    }

  }

  private def queryHandler: Behavior[GroupQuery] = ???
}
