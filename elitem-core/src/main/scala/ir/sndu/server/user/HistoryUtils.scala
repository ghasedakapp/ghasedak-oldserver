package ir.sndu.server.user

import java.time.LocalDateTime

import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.apimessage.ApiMessage
import ir.sndu.server.apipeer.ApiPeer
import ir.sndu.server.model.history.HistoryMessage
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
