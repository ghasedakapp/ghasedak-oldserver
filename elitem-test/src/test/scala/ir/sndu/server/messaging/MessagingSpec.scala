package ir.sndu.server.messaging

import java.time.Instant

import ir.sndu.server.GrpcBaseSuit
import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }

import scala.util.Random

class MessagingSpec extends GrpcBaseSuit
  with AuthHelper {

  "send message" should "" in sendMessage
  "load history" should "" in loadHistory

  def sendMessage(): Unit = {
    val UserInfo(user1, token1, _) = createUser()
    val UserInfo(user2, token2, _) = createUser()

    val request = RequestSendMessage(
      Some(ApiOutPeer(ApiPeerType.Private, user2.id)),
      Random.nextLong(),
      Some(ApiMessage().withTextMessage(ApiTextMessage("salam"))),
      token1)
    messagingStub.sendMessage(request) shouldBe ResponseVoid()
  }

  def loadHistory: Unit = {
    val UserInfo(user1, token1, _) = createUser()
    val UserInfo(user2, token2, _) = createUser()

    val outPeer2 = ApiOutPeer(ApiPeerType.Private, user2.id)

    def befrest(msg: ApiMessage): Unit = {
      messagingStub.sendMessage(RequestSendMessage(
        Some(outPeer2),
        Random.nextLong(),
        Some(msg),
        token1))
    }

    val msg1 = ApiMessage().withTextMessage(ApiTextMessage("salam1"))
    val msg2 = ApiMessage().withTextMessage(ApiTextMessage("salam2"))
    befrest(msg1)
    befrest(msg2)

    val rsp = messagingStub.loadHistory(RequestLoadHistory(
      Some(outPeer2), Instant.now.minusSeconds(3600L).toEpochMilli, 10, token1))

    rsp.history.size shouldBe 2
    rsp.history.map(_.message.get) shouldBe Seq(msg1, msg2)

  }

}
