package ir.sndu.server.model.dialog

import java.time.{ LocalDateTime, ZoneId }

import ir.sndu.api.messaging.{ ApiDialog, ApiMessage, ApiMessageContainer }
import ir.sndu.api.peer.ApiPeer
import ir.sndu.server.model.history.HistoryMessage

case class DialogCommon(
  dialogId:        String,
  lastMessageDate: LocalDateTime,
  lastMessageSeq:  Int,
  lastReceivedSeq: Int,
  lastReadSeq:     Int)

case class UserDialog(
  userId:               Int,
  peer:                 ApiPeer,
  ownerLastReceivedSeq: Int,
  ownerLastReadSeq:     Int,
  createdAt:            LocalDateTime)

case class Dialog(
  userId:               Int,
  peer:                 ApiPeer,
  ownerLastReceivedSeq: Int,
  ownerLastReadSeq:     Int,
  lastMessageSeq:       Int,
  lastMessageDate:      LocalDateTime,
  lastReceivedSeq:      Int,
  lastReadSeq:          Int,
  createdAt:            LocalDateTime) {
  def toApi(msgOpt: Option[HistoryMessage]): ApiDialog = {
    val history = msgOpt.getOrElse(HistoryMessage.empty(userId, peer, lastMessageDate))
    val msgDate = lastMessageDate.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
    ApiDialog(
      Some(peer),
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
      userId = dialog.userId,
      peer = dialog.peer,
      ownerLastReceivedSeq = dialog.ownerLastReceivedSeq,
      ownerLastReadSeq = dialog.ownerLastReadSeq,
      lastMessageDate = common.lastMessageDate,
      lastMessageSeq = common.lastMessageSeq,
      lastReceivedSeq = common.lastReceivedSeq,
      lastReadSeq = common.lastReadSeq,
      createdAt = dialog.createdAt)
}