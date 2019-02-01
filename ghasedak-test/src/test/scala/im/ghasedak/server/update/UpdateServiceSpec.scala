package im.ghasedak.server.update

import im.ghasedak.api.update.ApiUpdateContainer.Update.Pong
import im.ghasedak.api.update.{ ApiUpdateContainer, UpdatePong }
import im.ghasedak.rpc.test.RequestSendUpdate
import im.ghasedak.rpc.update.RequestGetState
import im.ghasedak.server.GrpcBaseSuit

import scala.util.Random

class UpdateServiceSpec extends GrpcBaseSuit {

  behavior of "UpdateServiceImpl"

  private val n = 10 // number of updates

  it should "get state without any update" in {
    val user = createUserWithPhone()
    val stub = updateStub.getState.addHeader(tokenMetadataKey, user.token)
    val seqState = stub.invoke(RequestGetState()).futureValue.seqState.get
    seqState.seq shouldEqual -1
  }

  it should "get state with n update" in {
    val user = createUserWithPhone()
    val stub1 = updateStub.getState.addHeader(tokenMetadataKey, user.token)
    val stub2 = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)
    val seqState1 = stub1.invoke(RequestGetState()).futureValue.seqState.get
    seqState1.seq shouldEqual -1
    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates foreach { update ⇒
      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
    }
    val seqState2 = stub1.invoke(RequestGetState()).futureValue.seqState.get
    seqState2.seq shouldEqual n - 1
  }

  it should "get one update after send it" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)

    stub.invoke(RequestSendUpdate(Some(ApiUpdateContainer().withPong(UpdatePong())))).futureValue

    {
      implicit val testUser: TestUser = user
      expectUpdate(classOf[Pong]) _
    }
  }

  it should "get n update after send it" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates foreach { update ⇒
      stub.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n)
    }
  }

  it should "get 2 * n update with two get difference" in {
    val user = createUserWithPhone()
    val stub1 = updateStub.getState.addHeader(tokenMetadataKey, user.token)
    val stub2 = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates1 foreach { update ⇒
      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n)
    }

    val seqState = stub1.invoke(RequestGetState()).futureValue.seqState.get
    seqState.seq shouldEqual n - 1

    val orderOfUpdates2 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates2 foreach { update ⇒
      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n, seqState)
    }

    val finalSeqState = stub1.invoke(RequestGetState()).futureValue.seqState.get
    finalSeqState.seq shouldEqual 2 * n - 1

    {
      implicit val testUser: TestUser = user
      expectNoUpdate(finalSeqState)
    }
  }

  it should "get n update with keep sending order" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates foreach { update ⇒
      stub.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectOrderUpdate(orderOfUpdates map (_.update))
    }
  }

  it should "send n update and don't get any update after that" in {
    val user = createUserWithPhone()
    val stub1 = updateStub.getState.addHeader(tokenMetadataKey, user.token)
    val stub2 = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates1 foreach { update ⇒
      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n)
    }

    val seqState = stub1.invoke(RequestGetState()).futureValue.seqState.get
    seqState.seq shouldEqual n - 1

    {
      implicit val testUser: TestUser = user
      expectNoUpdate(seqState)
    }
  }

  //  it should "send n update and don't get any update after that" in {
  //    val user = createUserWithPhone()
  //    val stub1 = updateStub.getState.addHeader(tokenMetadataKey, user.token)
  //    val stub2 = testStub.sendUpdate.addHeader(tokenMetadataKey, user.token)
  //
  //    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates1 foreach { update ⇒
  //      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectNStreamingUpdate(n)
  //    }
  //
  //    val seqState = stub1.invoke(RequestGetState()).futureValue.seqState.get
  //    seqState.seq shouldEqual n - 1
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectNoUpdate(seqState)
  //    }
  //  }

}
