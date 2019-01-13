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
    val stub = updateStub.withInterceptors(clientTokenInterceptor(user.token))
    val seqState = stub.getState(RequestGetState()).seqState.get
    seqState.seq shouldEqual -1
  }

  it should "get state with n update" in {
    val user = createUserWithPhone()
    val stub1 = updateStub.withInterceptors(clientTokenInterceptor(user.token))
    val stub2 = testStub.withInterceptors(clientTokenInterceptor(user.token))
    val seqState1 = stub1.getState(RequestGetState()).seqState.get
    seqState1.seq shouldEqual -1
    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates map { update ⇒
      stub2.sendUpdate(RequestSendUpdate(Some(update)))
    }
    val seqState2 = stub1.getState(RequestGetState()).seqState.get
    seqState2.seq shouldEqual n - 1
  }

  it should "get one update after send it" in {
    val user = createUserWithPhone()
    val stub = testStub.withInterceptors(clientTokenInterceptor(user.token))

    stub.sendUpdate(RequestSendUpdate(Some(ApiUpdateContainer().withPong(UpdatePong()))))

    {
      implicit val testUser: TestUser = user
      expectUpdate(classOf[Pong]) _
    }
  }

  it should "get n update after send it" in {
    val user = createUserWithPhone()
    val stub = testStub.withInterceptors(clientTokenInterceptor(user.token))

    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates map { update ⇒
      stub.sendUpdate(RequestSendUpdate(Some(update)))
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n)
    }
  }

  it should "get 2 * n update with two get difference" in {
    val user = createUserWithPhone()
    val stub1 = updateStub.withInterceptors(clientTokenInterceptor(user.token))
    val stub2 = testStub.withInterceptors(clientTokenInterceptor(user.token))

    val orderOfUpdates1 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates1 map { update ⇒
      stub2.sendUpdate(RequestSendUpdate(Some(update)))
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n)
    }

    val seqState = stub1.getState(RequestGetState()).seqState.get
    seqState.seq shouldEqual n - 1

    val orderOfUpdates2 = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates2 map { update ⇒
      stub2.sendUpdate(RequestSendUpdate(Some(update)))
    }

    {
      implicit val testUser: TestUser = user
      expectNUpdate(n, seqState)
    }

    val finalSeqState = stub1.getState(RequestGetState()).seqState.get
    finalSeqState.seq shouldEqual 2 * n - 1

    {
      implicit val testUser: TestUser = user
      expectNoUpdate(finalSeqState)
    }
  }

  it should "get n update with keep sending order" in {
    val user = createUserWithPhone()
    val stub = testStub.withInterceptors(clientTokenInterceptor(user.token))

    val orderOfUpdates = Seq.fill(n)(ApiUpdateContainer().withPong(UpdatePong(Random.nextInt())))
    orderOfUpdates map { update ⇒
      stub.sendUpdate(RequestSendUpdate(Some(update)))
    }

    {
      implicit val testUser: TestUser = user
      expectOrderUpdate(orderOfUpdates map (_.update))
    }
  }

}
