package ir.sndu.server.model.history

import java.time.LocalDateTime

import ir.sndu.server.peer.ApiPeer

case class HistoryMessage(
  userId: Int,
  peer: ApiPeer,
  date: LocalDateTime,
  senderUserId: Int,
  randomId: Long,
  messageContentHeader: Int,
  messageContentData: Array[Byte],
  deletedAt: Option[LocalDateTime]) {
  def ofUser(userId: Int) = this.copy(userId = userId)
}
