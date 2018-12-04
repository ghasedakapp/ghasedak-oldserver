package ir.sndu.server.messaging

import java.time.Instant

import ir.sndu.api.message._
import ir.sndu.api.peer._
import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.group.GroupHelper
import ir.sndu.rpc.messaging._
import ir.sndu.server._

import scala.util.Random

class MessagingSpec extends GrpcBaseSuit
  with AuthHelper
  with MessagingHelper
  with GroupHelper {

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
      Some(ApiMessage().withTextMessage(ApiTextMessage("salam"))))
    messagingStub.sendMessage(request) shouldBe ResponseVoid()
  }

  def sendGroupMessage(): Unit = {
    val clientData1 = createUser()
    val clientData2 = createUser()
    val ClientData(user1, token1, _) = clientData1
    val ClientData(user2, token2, _) = clientData2

    val apiGroup = {
      implicit val client = clientData1
      createGroup("Fun Group", Seq(ApiUserOutPeer(user2.id)))
    }

    val request = RequestSendMessage(
      Some(ApiOutPeer(ApiPeerType.Group, apiGroup.id)),
      Random.nextLong(),
      Some(ApiMessage().withTextMessage(ApiTextMessage("salam"))))
    messagingStub.sendMessage(request) shouldBe ResponseVoid()
  }

  def loadHistory: Unit = {
    implicit val clientData1 = createUser()
    val ClientData(user1, token1, _) = clientData1
    val ClientData(user2, token2, _) = createUser()

    val outPeer2 = ApiOutPeer(ApiPeerType.Private, user2.id)

    val msg1 = ApiMessage().withTextMessage(ApiTextMessage("salam1"))
    val msg2 = ApiMessage().withTextMessage(ApiTextMessage("salam2"))
    befrest(outPeer2, msg1)
    Thread.sleep(100)
    befrest(outPeer2, msg2)

    val rsp = messagingStub.loadHistory(RequestLoadHistory(
      Some(outPeer2), Instant.now.minusSeconds(3600L).toEpochMilli, 10))

    rsp.history.size shouldBe 2
    rsp.history.map(_.message.get) shouldBe Seq(msg1, msg2)

  }

}
