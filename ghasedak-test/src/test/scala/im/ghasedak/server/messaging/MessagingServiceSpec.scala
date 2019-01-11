package im.ghasedak.server.messaging

import im.ghasedak.api.messaging.{ ApiListLoadMode, ApiMessage, ApiTextMessage }
import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import im.ghasedak.rpc.messaging.{ RequestLoadDialogs, RequestLoadHistory, RequestSendMessage }
import im.ghasedak.server.GrpcBaseSuit

import scala.util.Random

class MessagingServiceSpec extends GrpcBaseSuit {

  behavior of "MessagingServiceImpl"

  it should "send message, load history, load dialog" in {
    val aliUser = createUserWithPhone()
    val aliPeer = Some(ApiPeer(ApiPeerType.PRIVATE, aliUser.userId))
    val hosseinUser = createUserWithPhone()
    val hosseinPeer = Some(ApiPeer(ApiPeerType.PRIVATE, hosseinUser.userId))
    val salehUser = createUserWithPhone()
    val salehPeer = Some(ApiPeer(ApiPeerType.PRIVATE, salehUser.userId))

    val stubAli = messagingStub.withInterceptors(clientTokenInterceptor(aliUser.token))
    val stubHossein = messagingStub.withInterceptors(clientTokenInterceptor(hosseinUser.token))

    val msgToHossein = ApiMessage().withTextMessage(ApiTextMessage("salamToHossein"))
    val msgToAli = ApiMessage().withTextMessage(ApiTextMessage("salamToAli"))
    val msgToSaleh = ApiMessage().withTextMessage(ApiTextMessage("salamToSaleh"))

    stubAli.sendMessage(RequestSendMessage(
      hosseinPeer, Random.nextLong(),
      Some(msgToHossein)))

    Thread.sleep(1000)

    stubHossein.sendMessage(RequestSendMessage(
      aliPeer, Random.nextLong(),
      Some(msgToAli)))

    val rspAli = stubAli.loadHistory(RequestLoadHistory(
      peer = hosseinPeer,
      sequenceNr = Int.MaxValue,
      limit = 10))
    rspAli.history.map(_.message.get) shouldBe Seq(msgToAli, msgToHossein)
    rspAli.history.map(_.sequenceNr) shouldBe Seq(2, 1)

    val rspHossein = stubHossein.loadHistory(RequestLoadHistory(
      peer = aliPeer,
      sequenceNr = Int.MaxValue,
      limit = 10))
    rspHossein.history.map(_.message.get) shouldBe Seq(msgToAli, msgToHossein)
    rspHossein.history.map(_.sequenceNr) shouldBe Seq(2, 1)

    Thread.sleep(1000)

    stubAli.sendMessage(RequestSendMessage(
      salehPeer, Random.nextLong(),
      Some(msgToSaleh)))
    val dialogRspAli = stubAli.loadDialogs(RequestLoadDialogs(Long.MaxValue, 10))
    dialogRspAli.dialogs.map(_.message.get.message.get) shouldBe Seq(msgToSaleh, msgToAli)
  }

}
