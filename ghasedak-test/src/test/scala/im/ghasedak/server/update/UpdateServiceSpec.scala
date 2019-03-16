package im.ghasedak.server.update

import im.ghasedak.api.update.ApiUpdateContainer.Update.Pong
import im.ghasedak.api.update.{ ApiUpdateContainer, UpdatePong }
import im.ghasedak.rpc.test.RequestSendUpdate
import im.ghasedak.server.GrpcBaseSuit

import scala.util.Random

class UpdateServiceSpec extends GrpcBaseSuit {

  behavior of "UpdateServiceImpl"

  private val n = 10 // number of updates

  private val updExt = SeqUpdateExtension(system)

  //  it should "get one update after send it" in {
  //    val user = createUserWithPhone()
  //    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)
  //
  //    stub.invoke(RequestSendUpdate(Some(ApiUpdateContainer().withPong(UpdatePong())))).futureValue
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamUpdate(classOf[Pong]) _
  //    }
  //  }
  //
  //  it should "get n update after send it" in {
  //    val user = createUserWithPhone()
  //    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)
  //
  //    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates foreach { update ⇒
  //      stub.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamNUpdate(n)
  //    }
  //  }
  //
  //  it should "get 2 * n update with two get difference" in {
  //    val user = createUserWithPhone()
  //    val stub2 = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)
  //
  //    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates1 foreach { update ⇒
  //      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamNUpdate(n)
  //    }
  //
  //    Thread.sleep(1000)
  //
  //    val orderOfUpdates2 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates2 foreach { update ⇒
  //      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamNUpdate(n)
  //    }
  //  }
  //
  //  it should "get n update with keep sending order" in {
  //    val user = createUserWithPhone()
  //    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)
  //
  //    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates foreach { update ⇒
  //      stub.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamOrderUpdate(orderOfUpdates map (_.update))
  //    }
  //  }
  //
  //  it should "send n update and don't get any update after that" in {
  //    val user = createUserWithPhone()
  //    val stub2 = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)
  //
  //    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates1 foreach { update ⇒
  //      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamNUpdate(n)
  //    }
  //
  //  }
  //
  //  it should "not receive acked update" in {
  //    val user = createUserWithPhone()
  //    val stub2 = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)
  //
  //    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
  //    orderOfUpdates1 foreach { update ⇒
  //      stub2.invoke(RequestSendUpdate(Some(update))).futureValue
  //    }
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamNUpdate(n)
  //    }
  //
  //    Thread.sleep(1000)
  //    updExt.stop(user.userId, user.tokenId)
  //    Thread.sleep(1000)
  //
  //    {
  //      implicit val testUser: TestUser = user
  //      expectStreamNoUpdate()
  //    }
  //  }

  it should "get no update" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)

    {
      implicit val testUser: TestUser = user
      expectNoUpdate()
    }
  }

  it should "get n update with batch size 1" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates1 foreach { update ⇒
      stub.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate()(n)
    }
  }

  it should "get n update with batch size 2" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates1 foreach { update ⇒
      stub.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(2)(n)
    }
  }

}
