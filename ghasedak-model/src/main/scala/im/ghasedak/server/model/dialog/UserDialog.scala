package im.ghasedak.server.model.dialog

import java.time.LocalDateTime

import im.ghasedak.api.messaging.{ Dialog, HistoryMessage }
import im.ghasedak.server.model.TimeConversions._
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

final case class DialogModel(
  chatId:               Long,
  ownerLastReceivedSeq: Int,
  ownerLastReadSeq:     Int,
  lastMessageSeq:       Int,
  lastMessageDate:      LocalDateTime,
  lastReceivedSeq:      Int,
  lastReadSeq:          Int,
  createdAt:            LocalDateTime) {

  def toApi(msgOpt: Option[HistoryMessage]): Dialog = {
    val history = msgOpt.getOrElse(HistoryMessage().copy(chatId = chatId, date = Some(lastMessageDate)))
    Dialog(
      chatId,
      lastMessageSeq - ownerLastReadSeq,
      Some(lastMessageDate),
      Some(history))
  }

}

object DialogModel {

  def from(common: DialogCommon, dialog: UserDialog): DialogModel =
    DialogModel(
      chatId = dialog.chatId,
      ownerLastReceivedSeq = dialog.ownerLastReceivedSeq,
      ownerLastReadSeq = dialog.ownerLastReadSeq,
      lastMessageDate = common.lastMessageDate,
      lastMessageSeq = common.lastMessageSeq,
      lastReceivedSeq = common.lastReceivedSeq,
      lastReadSeq = common.lastReadSeq,
      createdAt = dialog.createdAt)

}