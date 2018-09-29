package ir.sndu.server.messaging

import java.time.Instant

import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.message.{ ApiMessage, ApiTextMessage }
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }
import ir.sndu.server.{ GrpcBaseSuit, ClientData }

import scala.util.Random

class MessagingSpec extends GrpcBaseSuit
  with AuthHelper
  with MessagingHelper {
  behavior of "Messaging Service"
  it should "send private message" in sendPrivateMessage
  it should "send group message" in sendGroupMessage
  it should "load history" in loadHistory

  def sendPrivateMessage(): Unit = {
    val ClientData(user1, token1, _) = createUser()
    val ClientData(user2, token2, _) = createUser()

    val request = RequestSendMessage(
      Some(ApiOutPeer(ApiPeerType.Private, user2.id)),
      Random.nextLong(),
      Some(ApiMessage().withTextMessage(ApiTextMessage("salam"))),
      token1)
    messagingStub.sendMessage(request) shouldBe ResponseVoid()
  }


  def sendGroupMessage(): Unit = {
    val ClientData(user1, token1, _) = createUser()
    val ClientData(user2, token2, _) = createUser()

    val request = RequestSendMessage(
      Some(ApiOutPeer(ApiPeerType.Private, user2.id)),
      Random.nextLong(),
      Some(ApiMessage().withTextMessage(ApiTextMessage("salam"))),
      token1)
    messagingStub.sendMessage(request) shouldBe ResponseVoid()
  }

  def loadHistory: Unit = {
    implicit val userInfo = createUser()
    val ClientData(user1, token1, _) = userInfo
    val ClientData(user2, token2, _) = createUser()

    val outPeer2 = ApiOutPeer(ApiPeerType.Private, user2.id)

    val msg1 = ApiMessage().withTextMessage(ApiTextMessage("salam1"))
    val msg2 = ApiMessage().withTextMessage(ApiTextMessage("salam2"))
    befrest(outPeer2, msg1)
    Thread.sleep(100)
    befrest(outPeer2, msg2)

    val rsp = messagingStub.loadHistory(RequestLoadHistory(
      Some(outPeer2), Instant.now.minusSeconds(3600L).toEpochMilli, 10, token1))

    rsp.history.size shouldBe 2
    rsp.history.map(_.message.get) shouldBe Seq(msg1, msg2)

  }

}
