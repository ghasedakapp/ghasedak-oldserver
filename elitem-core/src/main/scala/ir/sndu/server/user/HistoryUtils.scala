package ir.sndu.server.user

import java.time.{ Instant, LocalDateTime, ZoneId }

import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.messaging.ApiMessage
import ir.sndu.server.model.history.HistoryMessage
import ir.sndu.server.peer.{ ApiPeer, ApiPeerType }
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object HistoryUtils {

  def writeHistoryMessage(
    origin: ApiPeer,
    dest: ApiPeer,
    randomId: Long,
    date: Instant,
    message: ApiMessage)(implicit ec: ExecutionContext): DBIO[Unit] = {
    val msgDate = LocalDateTime.ofInstant(date, ZoneId.systemDefault())
    for {
      _ <- HistoryMessageRepo.create(HistoryMessage(
        origin.id,
        dest,
        msgDate,
        origin.id,
        randomId,
        message.message.number,
        message.toByteArray,
        None))
      _ <- HistoryMessageRepo.create(HistoryMessage(
        dest.id,
        origin,
        msgDate,
        origin.id,
        randomId,
        message.message.number,
        message.toByteArray,
        None))
    } yield ()
  }
}
