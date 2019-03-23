package im.ghasedak.server.serializer

import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorRef, ActorRefResolver }

case class ActorRefSerializer[T]()(implicit system: akka.actor.ActorSystem) {
  private val actorRefResolver = ActorRefResolver(system.toTyped)

  def toString(ref: ActorRef[T]): String = actorRefResolver.toSerializationFormat(ref)
  def fromString(path: String): ActorRef[T] = actorRefResolver.resolveActorRef(path)
}
