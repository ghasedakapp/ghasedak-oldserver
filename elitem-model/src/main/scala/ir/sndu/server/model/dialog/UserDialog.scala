package ir.sndu.server.model.dialog

import java.time.LocalDateTime

import ir.sndu.server.model.Peer

case class DialogCommon(
  dialogId: String,
  lastMessageDate: LocalDateTime,
  lastReceivedAt: LocalDateTime,
  lastReadAt: LocalDateTime)

case class UserDialog(
  userId: Int,
  peer: Peer,
  ownerLastReceivedAt: LocalDateTime,
  ownerLastReadAt: LocalDateTime,
  createdAt: LocalDateTime,
  isFavourite: Boolean)