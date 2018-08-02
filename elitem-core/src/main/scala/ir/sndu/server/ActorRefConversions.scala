package ir.sndu.server

import akka.actor.typed.ActorRef

object ActorRefConversions {
  implicit def refToContainer[T](ref: ActorRef[T])(implicit system: akka.actor.ActorSystem): ActorRefContainer =
    ActorRefContainer(ActorRefSerializer().toString(ref))

}
