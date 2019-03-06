package im.ghasedak.server.utils

import java.util.concurrent._

import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Keep, Sink, Source }
import com.google.protobuf.ByteString
import com.sksamuel.pulsar4s.MessageId
import im.ghasedak.api.update._
import im.ghasedak.rpc.update._
import im.ghasedak.server.GrpcBaseSuit

trait UpdateMatcher {
  this: GrpcBaseSuit ⇒

  private type Update = ApiUpdateContainer.Update

  private type UpdateClass = Class[_ <: ApiUpdateContainer.Update]

  private val sleepForUpdates = 3000

  private val defaultSeqState = ApiSeqState(-1, ByteString.copyFrom(MessageId.earliest.bytes))

  def expectStreamUpdate(clazz: UpdateClass, seqState: ApiSeqState = defaultSeqState)(f: Update ⇒ Unit = _ ⇒ ())(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val latch = new CountDownLatch(1)
    var updateContainer = ApiUpdateContainer()
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .map { response ⇒
        if (response.receivedUpdates.head.getUpdateContainer.update.getClass == clazz) {
          updateContainer = response.receivedUpdates.head.getUpdateContainer
          latch.countDown()
        }
      }
      .runWith(Sink.ignore)(localMat)
    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
    f(updateContainer.update)
    localMat.shutdown()
  }

  def expectStreamNUpdate(n: Int, seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val latch = new CountDownLatch(n)
    val stub = updateStub.streamingGetDifference.addHeader(tokenMetadataKey, testUser.token)
    stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .map { _ ⇒
        latch.countDown()
      }
      .runWith(Sink.ignore)(localMat)
    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
    localMat.shutdown()
  }

  def expectNStreamingUpdate(n: Int, seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val latch = new CountDownLatch(n)
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    val s = Source.single(StreamingRequestGetDifference(Some(seqState)))
    stub.invoke(s)
      .map { _ ⇒
        latch.countDown()
      }
      .runWith(Sink.ignore)(localMat)
    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
    localMat.shutdown()
  }

  def expectStreamNoUpdate(seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    var hashAnyUpdate = false
    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    stub.invoke(Source.single(StreamingRequestGetDifference(Some(seqState))))
      .map { _ ⇒
        hashAnyUpdate = true
      }
      .runWith(Sink.ignore)(localMat)
    Thread.sleep(sleepForUpdates)
    assert(!hashAnyUpdate)
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
