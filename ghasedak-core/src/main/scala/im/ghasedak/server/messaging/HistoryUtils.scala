package im.ghasedak.server.messaging

import java.time.LocalDateTime

import im.ghasedak.api.messaging.ApiMessage
import im.ghasedak.server.model.history.HistoryMessage
import im.ghasedak.server.repo.history.HistoryMessageRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object HistoryUtils {

  def writeHistoryMessage(
    chatId:       Long,
    senderUserId: Int,
    randomId:     Long,
    date:         LocalDateTime,
    message:      ApiMessage)(implicit ec: ExecutionContext): DBIO[Int] = {
    for {
      seq ← HistoryMessageRepo.findNewest(chatId).map(_.map(_.sequenceNr).getOrElse(0))
      newSeq = seq + 1
      _ ← HistoryMessageRepo.create(HistoryMessage(
        chatId,
        date,
        senderUserId,
        newSeq,
        message.message.number,
        message.toByteArray,
        None))
    } yield newSeq
  }

}
