package ir.sndu.server.model.dialog

import java.time.{ LocalDateTime, ZoneId }

import ir.sndu.api.message._
import ir.sndu.api.peer.ApiPeer
import ir.sndu.server.model.history.HistoryMessage

case class DialogCommon(
  dialogId:        String,
  lastMessageDate: LocalDateTime,
  lastReceivedAt:  LocalDateTime,
  lastReadAt:      LocalDateTime)

case class UserDialog(
  userId:              Int,
  peer:                ApiPeer,
  ownerLastReceivedAt: LocalDateTime,
  ownerLastReadAt:     LocalDateTime,
  createdAt:           LocalDateTime,
  isFavourite:         Boolean)

case class Dialog(
  userId:              Int,
  peer:                ApiPeer,
  ownerLastReceivedAt: LocalDateTime,
  ownerLastReadAt:     LocalDateTime,
  lastMessageDate:     LocalDateTime,
  lastReceivedAt:      LocalDateTime,
  lastReadAt:          LocalDateTime,
  createdAt:           LocalDateTime,
  isFavourite:         Boolean) {
  def toApi(msgOpt: Option[HistoryMessage]): ApiDialog = {
    val history = msgOpt.getOrElse(HistoryMessage.empty(userId, peer, lastMessageDate))
    val msgDate = lastMessageDate.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
    ApiDialog(
      Some(peer),
      0,
      msgDate,
      history.senderUserId,
      history.randomId,
      msgDate,
      Some(ApiMessage.parseFrom(history.messageContentData)))
  }

}

object Dialog {
  def from(common: DialogCommon, dialog: UserDialog): Dialog =
    Dialog(
      userId = dialog.userId,
      peer = dialog.peer,
      ownerLastReceivedAt = dialog.ownerLastReceivedAt,
      ownerLastReadAt = dialog.ownerLastReadAt,
      lastMessageDate = common.lastMessageDate,
      lastReceivedAt = common.lastReceivedAt,
      lastReadAt = common.lastReadAt,
      createdAt = dialog.createdAt,
      isFavourite = dialog.isFavourite)
}