package ir.sndu.server.user

import java.time.{ Instant, LocalDateTime, ZoneId }

import ir.sndu.server.UserCommands.{ SendMessage, SendMessageAck }

import scala.concurrent.Future

trait UserCommandHandler {
  self: UserProcessor ⇒

  import DialogUtils._
  import HistoryUtils._

  def sendMessage(sm: SendMessage)(): Future[SendMessageAck] = {
    val msgDate = calculateDate
    val msgLocalDate = LocalDateTime.ofInstant(msgDate, ZoneId.systemDefault())
    val action = for {
      _ ← writeHistoryMessage(
        selfPeer,
        sm.peer.get,
        sm.randomId,
        msgLocalDate,
        sm.message.get)
      _ ← createOrUpdateDialog(userId, sm.peer.get, msgLocalDate)
      _ ← createOrUpdateDialog(sm.peer.get.id, selfPeer, msgLocalDate)
    } yield SendMessageAck()
    db.run(action)
  }

  private def calculateDate: Instant = {
    //TODO Avoids duplicate date
    Instant.now()
  }

}
