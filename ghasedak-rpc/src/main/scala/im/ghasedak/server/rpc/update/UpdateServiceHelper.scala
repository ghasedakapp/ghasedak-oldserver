package im.ghasedak.server.rpc.update

import akka.NotUsed
import akka.stream.scaladsl.Source
import im.ghasedak.api.update.SeqState
import im.ghasedak.rpc.update._
import im.ghasedak.server.update.UpdateHelper

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait UpdateServiceHelper extends UpdateHelper {
  this: UpdateServiceImpl ⇒

  def getDifference(userId: Int, tokenId: String, maxMessages: Int): Future[ResponseGetDifference] =
    seqUpdateExt.getDifference(userId, tokenId, maxMessages)

  def acknowledge(userId: Int, tokenId: String, seqState: Option[SeqState]): Future[Unit] =
    seqUpdateExt.acknowledge(userId, tokenId, seqState)

  def acknowledge(
    requestStream: Source[StreamingRequestGetDifference, NotUsed],
    userId:        Int, tokenId: String): Unit =
    requestStream.runForeach(req ⇒ acknowledge(userId, tokenId, req.ackId))
      .onComplete {
        case Success(_) ⇒ log.info("Ack request stream completed successfully")
        case Failure(e) ⇒ log.error(e, "Error in get diff request stream")
      }

  def seek(userId: Int, tokenId: String, state: Option[SeqState]): Future[Unit] = {
    seqUpdateExt.seek(userId, tokenId, state)
  }

}
