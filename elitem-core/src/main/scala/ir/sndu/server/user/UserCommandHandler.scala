package ir.sndu.server.user

import java.time.Instant

import ir.sndu.server.user.UserCommands.{ SendMessage, SendMessageAck }

import scala.concurrent.Future

trait UserCommandHandler {
  self: UserProcessor =>

  import HistoryUtils._

  def sendMessage(sm: SendMessage)(): Future[SendMessageAck] = {
    db.run((writeHistoryMessage _).tupled(SendMessage.unapply(
      sm.copy(date = Some(getDate))).get))
      .mapTo[SendMessageAck]
  }

  private def getDate: Long = {
    Instant.now().toEpochMilli
  }

}
