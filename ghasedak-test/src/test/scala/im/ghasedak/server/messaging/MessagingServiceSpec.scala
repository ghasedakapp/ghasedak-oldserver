package im.ghasedak.server.messaging

import im.ghasedak.api.messaging.{ ApiMessage, ApiTextMessage }
import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import im.ghasedak.rpc.messaging.{ RequestLoadDialogs, RequestLoadHistory, RequestMessageRead, RequestSendMessage }
import im.ghasedak.server.GrpcBaseSuit

import scala.util.Random

class MessagingServiceSpec extends GrpcBaseSuit {

  behavior of "MessagingServiceImpl"

  it should "send message, load history, load dialog" in sendMessage
  //  it should "read message" in readMessage

  def sendMessage: Unit = {
    val aliUser = createUserWithPhone()
    val aliPeer = Some(ApiPeer(ApiPeerType.PRIVATE, aliUser.userId))
    val hosseinUser = createUserWithPhone()
    val hosseinPeer = Some(ApiPeer(ApiPeerType.PRIVATE, hosseinUser.userId))
    val salehUser = createUserWithPhone()
    val salehPeer = Some(ApiPeer(ApiPeerType.PRIVATE, salehUser.userId))

    val stubAli = messagingStub.sendMessage().addHeader("token", aliUser.token)
    val loadHistoryStubAli = messagingStub.loadHistory().addHeader("token", aliUser.token)
    val loadDialogStubAli = messagingStub.loadDialogs().addHeader("token", aliUser.token)

    val stubHossein = messagingStub.sendMessage().addHeader("token", hosseinUser.token)
    val loadHistoryStubHossein = messagingStub.loadHistory().addHeader("token", hosseinUser.token)

    val msgToHossein = ApiMessage().withTextMessage(ApiTextMessage("salamToHossein"))
    val msgToAli = ApiMessage().withTextMessage(ApiTextMessage("salamToAli"))
    val msgToSaleh = ApiMessage().withTextMessage(ApiTextMessage("salamToSaleh"))

    stubAli.invoke(RequestSendMessage(
      hosseinPeer, Random.nextLong(),
      Some(msgToHossein))).futureValue

    Thread.sleep(1000)

    stubHossein.invoke(RequestSendMessage(
      aliPeer, Random.nextLong(),
      Some(msgToAli))).futureValue

    val rspAli = loadHistoryStubAli.invoke(RequestLoadHistory(
      peer = hosseinPeer,
      sequenceNr = Int.MaxValue,
      limit = 10)).futureValue
    rspAli.history.map(_.message.get) shouldBe Seq(msgToAli, msgToHossein)
    rspAli.history.map(_.sequenceNr) shouldBe Seq(2, 1)

    val rspHossein = loadHistoryStubHossein.invoke(RequestLoadHistory(
      peer = aliPeer,
      sequenceNr = Int.MaxValue,
      limit = 10)).futureValue
    rspHossein.history.map(_.message.get) shouldBe Seq(msgToAli, msgToHossein)
    rspHossein.history.map(_.sequenceNr) shouldBe Seq(2, 1)

    Thread.sleep(1000)

    stubAli.invoke(RequestSendMessage(
      salehPeer, Random.nextLong(),
      Some(msgToSaleh))).futureValue
    val dialogRspAli = loadDialogStubAli.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
    dialogRspAli.dialogs.map(_.message.get.message.get) shouldBe Seq(msgToSaleh, msgToAli)
  }

  def readMessage: Unit = {
    val aliUser = createUserWithPhone()
    val aliPeer = Some(ApiPeer(ApiPeerType.PRIVATE, aliUser.userId))
    val hosseinUser = createUserWithPhone()
    val hosseinPeer = Some(ApiPeer(ApiPeerType.PRIVATE, hosseinUser.userId))
    val salehUser = createUserWithPhone()
    val salehPeer = Some(ApiPeer(ApiPeerType.PRIVATE, salehUser.userId))

    val stubAli = messagingStub.sendMessage().addHeader("token", aliUser.token)
    val loadDialogStubAli = messagingStub.loadDialogs().addHeader("token", aliUser.token)

    val stubHossein = messagingStub.sendMessage().addHeader("token", hosseinUser.token)
    val loadDialogStubHossein = messagingStub.loadDialogs().addHeader("token", hosseinUser.token)
    val messageReadStubHossein = messagingStub.messageRead().addHeader("token", hosseinUser.token)

    val msgToHossein1 = ApiMessage().withTextMessage(ApiTextMessage("salamToHossein1"))
    val msgToHossein2 = ApiMessage().withTextMessage(ApiTextMessage("salamToHossein2"))

    val messageRsp1 = stubAli.invoke(RequestSendMessage(
      hosseinPeer, Random.nextLong(),
      Some(msgToHossein1))).futureValue

    Thread.sleep(1000)

    val messageRsp2 = stubAli.invoke(RequestSendMessage(
      hosseinPeer, Random.nextLong(),
      Some(msgToHossein2))).futureValue

    val dialogAliRsp1 = loadDialogStubAli.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
    dialogAliRsp1.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp1.sequenceNr)
    dialogAliRsp1.dialogs.head.sortDate shouldBe messageRsp2.date
    dialogAliRsp1.dialogs.head.unreadCount shouldBe 0

    val dialogHosseinRsp1 = loadDialogStubHossein.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
    dialogHosseinRsp1.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp1.sequenceNr)
    dialogHosseinRsp1.dialogs.head.sortDate shouldBe messageRsp2.date
    dialogHosseinRsp1.dialogs.head.unreadCount shouldBe 2

    messageReadStubHossein.invoke(RequestMessageRead(aliPeer, dialogHosseinRsp1.dialogs.head.message.get.sequenceNr)).futureValue

    val dialogAliRsp2 = loadDialogStubAli.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
    dialogAliRsp2.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp2.sequenceNr)

    val dialogHosseinRsp2 = loadDialogStubHossein.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
    dialogHosseinRsp2.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp2.sequenceNr)
    dialogHosseinRsp2.dialogs.head.sortDate shouldBe messageRsp2.date
    dialogHosseinRsp2.dialogs.head.unreadCount shouldBe 0
  }

}
