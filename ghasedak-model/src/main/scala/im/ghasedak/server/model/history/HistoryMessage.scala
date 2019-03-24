package im.ghasedak.server.model.history

import java.time.LocalDateTime

final case class HistoryMessage(
  chatId:               Long,
  date:                 LocalDateTime,
  senderUserId:         Int,
  sequenceNr:           Int,
  messageContentHeader: Int,
  messageContentData:   Array[Byte],
  deletedAt:            Option[LocalDateTime])

object HistoryMessage {

  def empty(chatId: Long, date: LocalDateTime) =
    HistoryMessage(chatId, date, 0, 0, 0, Array.emptyByteArray, None)

}
