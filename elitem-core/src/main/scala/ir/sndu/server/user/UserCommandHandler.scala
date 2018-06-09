package ir.sndu.server.user

import java.time.{ Instant, LocalDateTime, ZoneId, ZoneOffset }

import ir.sndu.persist.repo.dialog.DialogRepo
import ir.sndu.server.user.UserCommands.{ SendMessage, SendMessageAck }

import scala.concurrent.Future

trait UserCommandHandler {
  self: UserProcessor =>

  import HistoryUtils._
  import DialogUtils._

  def sendMessage(sm: SendMessage)(): Future[SendMessageAck] = {
    val msgDate = calculateDate
    val msgLocalDate = LocalDateTime.ofInstant(msgDate, ZoneOffset.UTC)
    val action = for {
      _ <- writeHistoryMessage(
        selfPeer,
        sm.peer.get,
        sm.randomId,
        msgDate,
        sm.message.get)
      _ <- createOrUpdateDialog(userId, sm.peer.get, msgLocalDate)
      _ <- createOrUpdateDialog(sm.peer.get.id, selfPeer, msgLocalDate)
    } yield SendMessageAck()
    db.run(action)
  }

  private def calculateDate: Instant = {
    //TODO Avoids duplicate date
    Instant.now()
  }

}
