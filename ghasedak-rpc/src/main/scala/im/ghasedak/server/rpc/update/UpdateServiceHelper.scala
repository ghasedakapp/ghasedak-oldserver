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

  def getDifference(userId: Int, tokenId: String): Future[ResponseGetDifference] = {
    seqUpdateExt.getDifference(userId, tokenId)
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def acknowledge(userId: Int, tokenId: String, seqState: Option[ApiSeqState]): Future[Unit] = {
    Future.successful()
    //    seqState match {
    //      case Some(state) ⇒
    //        val messageId = getMessageId(state)
    //        seqUpdateExt.acknowledge(userId, tokenId, messageId)
    //      case None ⇒ Future.successful()
    //    }
  }

  def acknowledge(
    requestStream: Source[StreamingRequestGetDifference, NotUsed],
    userId:        Int, tokenId: String): Unit =
    requestStream.runForeach(req ⇒ acknowledge(userId, tokenId, req.ackId))
      .onComplete {
        case Success(_) ⇒ log.info("Get diff request stream completed successfully")
        case Failure(e) ⇒ log.error(e, "Error in get diff request stream")
      }

  def seek(userId: Int, tokenId: String, state: ApiSeqState): Future[Unit] = {
    val messageId = getMessageId(state)
    seqUpdateExt.seek(userId, tokenId, messageId)
  }

}
