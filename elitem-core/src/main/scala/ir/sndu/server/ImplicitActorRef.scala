package ir.sndu.server

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
object ImplicitActorRef {

  implicit class ExtActorRef[T](ref: ActorRef[T])(implicit system: ActorSystem) {
    def toContainer: ActorRefContainer = ActorRefContainer(ActorRefSerializer().toString(ref))
  }

  implicit class ExtActorRefContainer[T](container: ActorRefContainer)(implicit system: ActorSystem) {
    def !(msg: T) = ActorRefSerializer[T]().fromString(container.ref) ! msg
  }
}
