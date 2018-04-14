package ir.sndu.server.model.dialog

import java.time.{ LocalDateTime, ZoneOffset }

import ir.sndu.server.messaging.ApiDialog
import ir.sndu.server.peer.ApiPeer

case class DialogCommon(
  dialogId: String,
  lastMessageDate: LocalDateTime,
  lastReceivedAt: LocalDateTime,
  lastReadAt: LocalDateTime)

case class UserDialog(
  userId: Int,
  peer: ApiPeer,
  ownerLastReceivedAt: LocalDateTime,
  ownerLastReadAt: LocalDateTime,
  createdAt: LocalDateTime,
  isFavourite: Boolean)

case class Dialog(
  userId: Int,
  peer: ApiPeer,
  ownerLastReceivedAt: LocalDateTime,
  ownerLastReadAt: LocalDateTime,
  lastMessageDate: LocalDateTime,
  lastReceivedAt: LocalDateTime,
  lastReadAt: LocalDateTime,
  createdAt: LocalDateTime,
  isFavourite: Boolean) {
  def toApi: ApiDialog = ApiDialog(
    Some(peer),
    0,
    lastMessageDate.toEpochSecond(ZoneOffset.UTC))

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