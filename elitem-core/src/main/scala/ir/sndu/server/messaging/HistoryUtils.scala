package ir.sndu.server.messaging

import java.time.LocalDateTime

import im.ghasedak.api.messaging.ApiMessage
import im.ghasedak.api.peer.ApiPeer
import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.model.history.HistoryMessage
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object HistoryUtils {

  def writeHistoryMessage(
    origin:   ApiPeer,
    dest:     ApiPeer,
    randomId: Long,
    date:     LocalDateTime,
    message:  ApiMessage)(implicit ec: ExecutionContext): DBIO[Int] = {
    for {
      seq ← HistoryMessageRepo.findNewest(origin.id, dest).map(_.map(_.sequenceNr).getOrElse(0))
      newSeq = seq + 1
      _ ← HistoryMessageRepo.create(HistoryMessage(
        origin.id,
        dest,
        date,
        origin.id,
        newSeq,
        message.message.number,
        message.toByteArray,
        None))
      _ ← HistoryMessageRepo.create(HistoryMessage(
        dest.id,
        origin,
        date,
        origin.id,
        newSeq,
        message.message.number,
        message.toByteArray,
        None))
    } yield newSeq
  }

}
