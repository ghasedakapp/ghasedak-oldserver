package ir.sndu.server.group

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.event.Logging
import ir.sndu.server.group.GroupCommands.{ Create, CreateAck }

object GroupProcessor {
  import ir.sndu.server.ImplicitActorRef._

  val behavior: Behavior[GroupCommand] = commandHandler

  private def commandHandler: Behavior[GroupCommand] = {
    Behaviors.receive { (ctx, msg) ⇒
      implicit val system = ctx.system.toUntyped
      val log = Logging(system, getClass)
      msg match {
        case c: Create ⇒
          c.replyTo ! CreateAck()
          Behaviors.same
      }
    }

  }

  private def queryHandler: Behavior[GroupQuery] = ???
}
