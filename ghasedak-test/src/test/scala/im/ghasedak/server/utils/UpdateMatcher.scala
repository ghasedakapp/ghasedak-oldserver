package im.ghasedak.server.utils

import java.util.concurrent._

import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Keep, Sink, Source }
import akka.stream.testkit.scaladsl.TestSink
import com.google.protobuf.ByteString
import com.sksamuel.pulsar4s.MessageId
import im.ghasedak.api.update._
import im.ghasedak.rpc.update._
import im.ghasedak.server.GrpcBaseSuit
import scala.concurrent.duration._

trait UpdateMatcher {
  this: GrpcBaseSuit ⇒

  private type Update = ApiUpdateContainer.Update

  private type UpdateClass = Class[_ <: ApiUpdateContainer.Update]

  private val sleepForUpdates = 3000

  private val defaultSeqState = ApiSeqState(-1, ByteString.copyFrom(MessageId.earliest.bytes))

  def expectStreamUpdate(clazz: UpdateClass, seqState: ApiSeqState = defaultSeqState)(f: Update ⇒ Unit = _ ⇒ ())(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    val src = stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .filter(_.receivedUpdates.head.getUpdateContainer.update.getClass == clazz)
      .runWith(TestSink.probe[StreamingResponseGetDifference])(localMat)

    val result = src
      .request(1)
      .expectNextN(1)

    src
      .request(1)
      .expectNoMessage(3.seconds)

    f(result.head.receivedUpdates.head.getUpdateContainer.update)
    localMat.shutdown()
  }

  def expectStreamNUpdate(n: Int, seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    val src = stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .runWith(TestSink.probe[StreamingResponseGetDifference])(localMat)

    src
      .request(n)
      .expectNextN(n)

    src
      .request(1)
      .expectNoMessage(3.seconds)

    localMat.shutdown()
  }

  def expectStreamNoUpdate(seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    val src = stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .runWith(TestSink.probe[StreamingResponseGetDifference])(localMat)
    src
      .request(1)
      .expectNoMessage(3.seconds)
    localMat.shutdown()
  }

  def expectStreamOrderUpdate(orderOfUpdates: Seq[Update], seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val latch = new CountDownLatch(orderOfUpdates.length)
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .zipWithIndex.map {
        case (response, index) ⇒
          if (orderOfUpdates(index.toInt) == response.receivedUpdates.head.getUpdateContainer.update)
            latch.countDown()
      }
      .runWith(Sink.ignore)(localMat)
    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
    localMat.shutdown()
  }

}
