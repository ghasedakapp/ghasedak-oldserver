package ir.sndu.server.user

import ir.sndu.server.messaging.ApiMessage
import ir.sndu.server.peer.ApiPeer
import slick.dbio.DBIO

object HistoryUtils {

  def writeHistoryMessage(
    userId: Int = 0,
    peer: Option[ApiPeer] = None,
    randomId: Long = 0L,
    date: Option[Long] = None,
    message: scala.Option[ApiMessage] = None): DBIO[Unit] = {
    null
  }
}
