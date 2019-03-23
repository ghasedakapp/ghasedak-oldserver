package im.ghasedak.server.update

import im.ghasedak.api.update.{ ApiUpdateContainer, UpdatePong }
import im.ghasedak.rpc.test.RequestSendUpdate
import im.ghasedak.server.GrpcBaseSuit

import scala.util.Random

class GetDifferenceSpec extends GrpcBaseSuit {

  behavior of "GetDifferenceSpec"

  private val n = 10 // number of updates

  private val updExt = SeqUpdateExtension(system)

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

  it should "seek to old update" in {
    val user = createUserWithPhone()
    val stub = testStub.sendUpdate().addHeader(tokenMetadataKey, user.token)

    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates1 foreach { update ⇒
      stub.invoke(RequestSendUpdate(Some(update))).futureValue
    }

    {
      implicit val testUser: TestUser = user
      val ids = expectNUpdate()(n, ack = false)
      expectNoUpdate()
      seek(ids(5))
      expectNUpdate()(n - 5)
      expectNoUpdate()
    }

  }

}
