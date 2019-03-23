package im.ghasedak.server.rpc.update

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.ConsumerMessage
import com.sksamuel.pulsar4s.akka.streams.Control
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update._
import im.ghasedak.server.update.{ UpdateHelper, UpdateMapping }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait UpdateServiceHelper extends UpdateHelper {
  this: UpdateServiceImpl ⇒

  def getDifference(userId: Int, tokenId: String, maxMessages: Int): Future[ResponseGetDifference] =
    seqUpdateExt.getDifference(userId, tokenId, maxMessages)

  def acknowledge(userId: Int, tokenId: String, seqState: Option[ApiSeqState]): Future[Unit] =
    seqUpdateExt.acknowledge(userId, tokenId, seqState)

  def acknowledge(
    requestStream: Source[StreamingRequestGetDifference, NotUsed],
    userId:        Int, tokenId: String): Unit =
    requestStream.runForeach(req ⇒ acknowledge(userId, tokenId, req.ackId))
      .onComplete {
        case Success(_) ⇒ log.info("Ack request stream completed successfully")
        case Failure(e) ⇒ log.error(e, "Error in get diff request stream")
      }

  def seek(userId: Int, tokenId: String, state: Option[ApiSeqState]): Future[Unit] = {
    seqUpdateExt.seek(userId, tokenId, state)
  }

}
