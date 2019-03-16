package im.ghasedak.server.utils

import java.util.concurrent._

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, Sink, Source }
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ ActorMaterializer, OverflowStrategy }
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

  private def getAckActorStream: (ActorRef, Source[StreamingRequestGetDifference, NotUsed]) = {
    Source.actorRef[StreamingRequestGetDifference](8, OverflowStrategy.fail)
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
      .run()
  }

  def expectStreamUpdate(clazz: UpdateClass, seqState: ApiSeqState = defaultSeqState)(f: Update ⇒ Unit = _ ⇒ ())(implicit testUser: TestUser): Unit = {
    val localMat = ActorMaterializer()
    val (ackRef, source) = getAckActorStream

    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    val src = stub.invoke(source)
      .filter(_.receivedUpdates.head.getUpdateContainer.update.getClass == clazz)
      .runWith(TestSink.probe[StreamingResponseGetDifference])(localMat)

    val res = src
      .request(1)
      .expectNextN(1)

    ackRef ! StreamingRequestGetDifference(res.last.receivedUpdates.last.messageId)

    src
      .request(1)
      .expectNoMessage(3.seconds)

    f(res.head.receivedUpdates.head.getUpdateContainer.update)
    localMat.shutdown()
  }

  def seek(seqState: ApiSeqState)(implicit testUser: TestUser): Unit = {
    val stub = updateStub.seek().addHeader(tokenMetadataKey, testUser.token)
    stub.invoke(RequestSeek(Some(seqState))).futureValue
  }

  def expectNUpdate(batchSize: Int = 1)(n: Int, seqState: ApiSeqState = defaultSeqState, ack: Boolean = true)(implicit testUser: TestUser): Seq[ApiSeqState] = {
    val stub = updateStub.getDifference().addHeader(tokenMetadataKey, testUser.token)
    val ackStub = updateStub.acknowledge().addHeader(tokenMetadataKey, testUser.token)
    val result = (1 to n) flatMap (_ ⇒ stub.invoke(
      RequestGetDifference(
        maxMessages = batchSize)).futureValue.receivedUpdates)
    result.size shouldBe n
    if (ack) ackStub.invoke(RequestAcknowledge(result.last.messageId))
    result.flatMap(_.messageId)
  }

  def expectNoUpdate(batchSize: Int = 1)(implicit testUser: TestUser): Unit = {
    val stub = updateStub.getDifference().addHeader(tokenMetadataKey, testUser.token)
    val result = stub.invoke(RequestGetDifference(maxMessages = batchSize)).futureValue.receivedUpdates
    result.size shouldBe 0
  }

  def expectStreamNUpdate(n: Int, ack: Boolean = true)(implicit testUser: TestUser): Seq[ApiSeqState] = {
    val localMat = ActorMaterializer()
    val (ackRef, source) = getAckActorStream
    Thread.sleep(1000)

    val stub = updateStub.streamingGetDifference().addHeader(tokenMetadataKey, testUser.token)
    val src = stub.invoke(source)
      .runWith(TestSink.probe[StreamingResponseGetDifference])(localMat)

    val res = src
      .request(n)
      .expectNextN(n)

    if (ack) ackRef ! StreamingRequestGetDifference(res.last.receivedUpdates.last.messageId)

    src
      .request(1)
      .expectNoMessage(3.seconds)

    localMat.shutdown()
    res.flatMap(_.receivedUpdates.flatMap(_.messageId))
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
