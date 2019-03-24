package im.ghasedak.server.model.dialog

import java.time.{ LocalDateTime, ZoneOffset }

import im.ghasedak.api.messaging.{ ApiDialog, ApiMessage, ApiMessageContainer }
import im.ghasedak.server.model.history.HistoryMessage

final case class DialogCommon(
  chatId:          Long,
  lastMessageDate: LocalDateTime,
  lastMessageSeq:  Int,
  lastReceivedSeq: Int,
  lastReadSeq:     Int)

final case class UserDialog(
  userId:               Int,
  chatId:               Long,
  ownerLastReceivedSeq: Int,
  ownerLastReadSeq:     Int,
  createdAt:            LocalDateTime)

final case class Dialog(
  chatId:               Long,
  ownerLastReceivedSeq: Int,
  ownerLastReadSeq:     Int,
  lastMessageSeq:       Int,
  lastMessageDate:      LocalDateTime,
  lastReceivedSeq:      Int,
  lastReadSeq:          Int,
  createdAt:            LocalDateTime) {

  def toApi(msgOpt: Option[HistoryMessage]): ApiDialog = {
    val history = msgOpt.getOrElse(HistoryMessage.empty(chatId, lastMessageDate))
    val msgDate = lastMessageDate.toInstant(ZoneOffset.UTC).toEpochMilli
    ApiDialog(
      chatId,
      lastMessageSeq - ownerLastReadSeq,
      msgDate,
      Some(ApiMessageContainer(
        history.senderUserId,
        history.sequenceNr,
        msgDate,
        Some(ApiMessage.parseFrom(history.messageContentData)))))
  }

}

object Dialog {

  def from(common: DialogCommon, dialog: UserDialog): Dialog =
    Dialog(
      chatId = dialog.chatId,
      ownerLastReceivedSeq = dialog.ownerLastReceivedSeq,
      ownerLastReadSeq = dialog.ownerLastReadSeq,
      lastMessageDate = common.lastMessageDate,
      lastMessageSeq = common.lastMessageSeq,
      lastReceivedSeq = common.lastReceivedSeq,
      lastReadSeq = common.lastReadSeq,
      createdAt = dialog.createdAt)

}