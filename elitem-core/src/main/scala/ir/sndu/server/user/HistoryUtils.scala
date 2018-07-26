package ir.sndu.server.user

import java.time.{ Instant, LocalDateTime, ZoneId, ZoneOffset }

import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.message.ApiMessage
import ir.sndu.server.model.history.HistoryMessage
import ir.sndu.server.peer.ApiPeer
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object HistoryUtils {

  def writeHistoryMessage(
    origin:   ApiPeer,
    dest:     ApiPeer,
    randomId: Long,
    date:     LocalDateTime,
    message:  ApiMessage)(implicit ec: ExecutionContext): DBIO[Unit] = {
    for {
      _ ← HistoryMessageRepo.create(HistoryMessage(
        origin.id,
        dest,
        date,
        origin.id,
        randomId,
        message.message.number,
        message.toByteArray,
        None))
      _ ← HistoryMessageRepo.create(HistoryMessage(
        dest.id,
        origin,
        date,
        origin.id,
        randomId,
        message.message.number,
        message.toByteArray,
        None))
    } yield ()
  }
}
