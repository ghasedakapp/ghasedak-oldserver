package im.ghasedak.server.update

import akka.actor.Actor
import im.ghasedak.server.update.UpdateEnvelope.Deliver

class UpdateManager extends Actor {
  override def receive: Receive = {
    case Deliver ⇒
  }
}
