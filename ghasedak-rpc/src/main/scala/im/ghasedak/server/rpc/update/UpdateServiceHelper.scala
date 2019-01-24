package im.ghasedak.server.rpc.update

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.pulsar4s.{ ConsumerMessage, Reader }
import im.ghasedak.api.update.ApiSeqState
import im.ghasedak.rpc.update.ResponseGetDifference
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.update.UpdateMapping
import io.grpc.stub.StreamObserver

trait UpdateServiceHelper {
  this: UpdateServiceImpl ⇒

  // todo: remove this after akka grpc
  def getDifference(
    userId:           Int,
    tokenId:          String,
    seqState:         ApiSeqState,
    responseObserver: StreamObserver[ResponseGetDifference]): Unit = {
    val messageId = seqUpdateExt.getMessageId(seqState)
    val reader = seqUpdateExt.getUserDifferenceReader(userId, messageId)
    getDifference(userId, tokenId, reader, responseObserver)
  }

  // todo: remove this after akka grpc
  def getDifference(
    userId:           Int,
    tokenId:          String,
    reader:           Reader[UpdateMapping],
    responseObserver: StreamObserver[ResponseGetDifference]): Unit = {
    seqUpdateExt.readNextDifference(reader) map { consumerMessage ⇒
      sendDifference(userId, tokenId, reader, responseObserver, consumerMessage)
    } recover {
      case ex: Throwable ⇒
        log.error(ex, "Failed to get difference for userId: {}, tokenId: {} from pulsar", userId, tokenId)
        responseObserver.onError(CommonRpcErrors.InternalError)
        reader.closeAsync
    }
  }

  // todo: remove this after akka grpc
  def sendDifference(
    userId:           Int,
    tokenId:          String,
    reader:           Reader[UpdateMapping],
    responseObserver: StreamObserver[ResponseGetDifference],
    consumerMessage:  ConsumerMessage[UpdateMapping]): Unit = {
    val response = buildDifference(tokenId, consumerMessage)
    try {
      responseObserver.onNext(response)
      getDifference(userId, tokenId, reader, responseObserver)
    } catch {
      case ex: Throwable ⇒
        log.warning(ex.getMessage)
        reader.closeAsync
    }
  }

  def getDifference(
    userId:   Int,
    tokenId:  String,
    seqState: ApiSeqState): Source[ResponseGetDifference, NotUsed] = {
    val messageId = seqUpdateExt.getMessageId(seqState)
    seqUpdateExt.getDifference(userId, messageId)
      .map(cm ⇒ buildDifference(tokenId, cm))
  }

  def buildDifference(tokenId: String, consumerMessage: ConsumerMessage[UpdateMapping]): ResponseGetDifference = {
    val seqState = seqUpdateExt.getApiSeqState(consumerMessage.messageId)
    val updateMapping = consumerMessage.value
    val updateContainer = if (updateMapping.custom.keySet.contains(tokenId))
      updateMapping.custom(tokenId)
    else
      updateMapping.default.get
    ResponseGetDifference(Some(seqState), Some(updateContainer))
  }

}
