package ir.sndu.server.user

import java.time.Instant

import ir.sndu.server.user.UserCommands.{ SendMessage, SendMessageAck }

import scala.concurrent.Future

trait UserCommandHandler {
  self: UserProcessor =>

  import HistoryUtils._

  def sendMessage(sm: SendMessage)(): Future[SendMessageAck] = {
    db.run(writeHistoryMessage(
      selfPeer,
      sm.peer.get,
      sm.randomId,
      getDate,
      sm.message.get))
      .mapTo[SendMessageAck]
  }

  private def getDate: Instant = {
    Instant.now()
  }

}
