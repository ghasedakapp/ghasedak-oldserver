package ir.sndu.server.kernel

import akka.actor.{ Actor, Props }
import akka.actor.Actor.Receive

object UserProcessor {
  def props = Props(classOf[UserProcessor])
}

class UserProcessor extends Actor {
  override def receive: Receive = ???
}
