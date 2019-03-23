package im.ghasedak.server.serializer

import akka.actor.{ ActorSystem, Status }
import akka.actor.typed.ActorRef

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
object ImplicitActorRef {

  implicit class ExtActorRef[T](ref: ActorRef[T])(implicit system: ActorSystem) {
    def toContainer: ActorRefContainer = ActorRefContainer(ActorRefSerializer().toString(ref))
  }

  implicit class ExtActorRefContainer[T](container: ActorRefContainer)(implicit system: ActorSystem) {
    def !(msg: T) = ActorRefSerializer[T]().fromString(container.ref) ! msg
  }

  implicit class ExtPipeToSupport[T](future: Future[T])(implicit system: ActorSystem, ec: ExecutionContext) {
    def pipeTo(recipient: ActorRef[T]): Future[T] = {
      future andThen {
        case Success(r) ⇒ recipient ! r
        //        case Failure(f) ⇒ recipient ! Status.Failure(f)
      }
    }
  }
}
