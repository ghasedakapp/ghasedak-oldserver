package ir.sndu.server.user

import akka.actor.{ Actor, Props }
import ir.sndu.server.user.UserCommands.SendMessage

object UserProcessor {
  def props = Props(classOf[UserProcessor])
}

class UserProcessor extends Actor {
  private val userId = self.path.name.toInt

  override def receive: Receive = commandHandler

  private def commandHandler: Receive = {
    case SendMessage(_, Some(peer)) => println(peer)
  }

  //  private def query: Receive = {
  //
  //  }
}
