package ir.sndu.server.messaging

import ir.sndu.server.GrpcBaseSuit
import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }

import scala.util.Random

class MessagingSpec extends GrpcBaseSuit
  with AuthHelper {

  "send message" should "" in sendMessage

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

}
