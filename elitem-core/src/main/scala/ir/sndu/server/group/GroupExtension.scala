package ir.sndu.server.group

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId }
import akka.util.Timeout
import ir.sndu.server.ActorRefConversions._
import ir.sndu.server.group.GroupCommands.{ Create, CreateAck }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
class GroupExtensionImpl(system: ActorSystem) extends Extension {
  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler = system.scheduler
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val _sys = system

  val ref = system.spawn(GroupProcessor.behavior, "test")

  private def construct(r: ActorRef[CreateAck]): Create = Create(replyTo = r)

  val f: Future[CreateAck] = ref ? construct

}

object GroupExtension extends ExtensionId[GroupExtensionImpl] {
  override def createExtension(system: ExtendedActorSystem): GroupExtensionImpl = new GroupExtensionImpl(system)
}
