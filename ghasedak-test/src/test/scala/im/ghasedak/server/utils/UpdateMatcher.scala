//package im.ghasedak.server.utils
//
//import java.util.concurrent.{ CountDownLatch, TimeUnit }
//
//import com.google.protobuf.ByteString
//import com.sksamuel.pulsar4s.MessageId
//import im.ghasedak.api.update._
//import im.ghasedak.rpc.update._
//import im.ghasedak.server.GrpcBaseSuit
//import io.grpc.stub.StreamObserver
//
//trait UpdateMatcher {
//  this: GrpcBaseSuit ⇒
//
//  private type Update = ApiUpdateContainer.Update
//
//  private type UpdateClass = Class[_ <: ApiUpdateContainer.Update]
//
//  private val sleepForUpdates = 3000
//
//  private val defaultSeqState = ApiSeqState(-1, ByteString.copyFrom(MessageId.earliest.bytes))
//
//  def expectUpdate(clazz: UpdateClass, seqState: ApiSeqState = defaultSeqState)(f: Update ⇒ Unit = _ ⇒ ())(implicit testUser: TestUser): Unit = {
//    val latch = new CountDownLatch(1)
//    var updateContainer = ApiUpdateContainer()
//    val stub = asyncUpdateStub.withInterceptors(clientTokenInterceptor(testUser.token))
//    val streamObserver = new StreamObserver[ResponseGetDifference] {
//      override def onError(t: Throwable): Unit = {}
//
//      override def onCompleted(): Unit = {}
//
//      override def onNext(value: ResponseGetDifference): Unit = {
//        if (value.getUpdateContainer.update.getClass == clazz) {
//          updateContainer = value.getUpdateContainer
//          latch.countDown()
//        }
//      }
//    }
//    stub.getDifference(RequestGetDifference(Some(seqState)), streamObserver)
//    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
//    f(updateContainer.update)
//  }
//
//  def expectNUpdate(n: Int, seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
//    val latch = new CountDownLatch(n)
//    val stub = asyncUpdateStub.withInterceptors(clientTokenInterceptor(testUser.token))
//    val streamObserver = new StreamObserver[ResponseGetDifference] {
//      override def onError(t: Throwable): Unit = {}
//
//      override def onCompleted(): Unit = {}
//
//      override def onNext(value: ResponseGetDifference): Unit = {
//        latch.countDown()
//      }
//    }
//    stub.getDifference(RequestGetDifference(Some(seqState)), streamObserver)
//    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
//  }
//
//  def expectNoUpdate(seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
//    var hashAnyUpdate = false
//    val stub = asyncUpdateStub.withInterceptors(clientTokenInterceptor(testUser.token))
//    val streamObserver = new StreamObserver[ResponseGetDifference] {
//      override def onError(t: Throwable): Unit = {}
//
//      override def onCompleted(): Unit = {}
//
//      override def onNext(value: ResponseGetDifference): Unit = {
//        hashAnyUpdate = true
//      }
//    }
//    stub.getDifference(RequestGetDifference(Some(seqState)), streamObserver)
//    Thread.sleep(sleepForUpdates)
//    assert(!hashAnyUpdate)
//  }
//
//  def expectOrderUpdate(orderOfUpdates: Seq[Update], seqState: ApiSeqState = defaultSeqState)(implicit testUser: TestUser): Unit = {
//    var orderIndex = 0
//    val latch = new CountDownLatch(orderOfUpdates.length)
//    val stub = asyncUpdateStub.withInterceptors(clientTokenInterceptor(testUser.token))
//    val streamObserver = new StreamObserver[ResponseGetDifference] {
//      override def onError(t: Throwable): Unit = {}
//
//      override def onCompleted(): Unit = {}
//
//      override def onNext(value: ResponseGetDifference): Unit = {
//        if (orderOfUpdates(orderIndex) == value.getUpdateContainer.update)
//          latch.countDown()
//        orderIndex += 1
//      }
//    }
//    stub.getDifference(RequestGetDifference(Some(seqState)), streamObserver)
//    assert(latch.await(sleepForUpdates, TimeUnit.MILLISECONDS))
//  }
//
//}
