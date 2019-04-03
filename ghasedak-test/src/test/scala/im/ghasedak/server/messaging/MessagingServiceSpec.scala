package im.ghasedak.server.messaging

import im.ghasedak.api.messaging._
import im.ghasedak.server.GrpcBaseSuit

class MessagingServiceSpec extends GrpcBaseSuit
  with MessagingHelper {

  behavior of "MessagingServiceImpl"

  it should "send private message" in privateMessage

  //  it should "read message" in readMessage

  def privateMessage: Unit = {
    val aliUser = createUserWithPhone()
    val hosseinUser = createUserWithPhone()

    val msgToHossein = MessageContent().withTextMessage(TextMessage("salamToHossein"))
    val msgToAli = MessageContent().withTextMessage(TextMessage("salamToAli"))

    val chat = sendPrivateMessage(aliUser.token, msgToHossein, hosseinUser.userId)
    sendPrivateMessage(hosseinUser.token, msgToAli, aliUser.userId, Some(chat))

    val rspHossein = loadHistory(hosseinUser.token, chat)
    rspHossein.history.map(_.message.get) shouldBe Seq(msgToAli, msgToHossein)
    rspHossein.history.map(_.senderUserId) shouldBe Seq(hosseinUser.userId, aliUser.userId)
    rspHossein.history.map(_.sequenceNr) shouldBe Seq(2, 1)

    val rspAli = loadHistory(aliUser.token, chat)
    rspAli.history.map(_.message.get) shouldBe Seq(msgToAli, msgToHossein)
    rspAli.history.map(_.senderUserId) shouldBe Seq(hosseinUser.userId, aliUser.userId)
    rspAli.history.map(_.sequenceNr) shouldBe Seq(2, 1)

  }

  //  def readMessage(): Unit = {
  //    val aliUser = createUserWithPhone()
  //    val aliPeer = Some(Peer(PeerType.PRIVATE, aliUser.userId))
  //    val hosseinUser = createUserWithPhone()
  //    val hosseinPeer = Some(Peer(PeerType.PRIVATE, hosseinUser.userId))
  //    val salehUser = createUserWithPhone()
  //    val salehPeer = Some(Peer(PeerType.PRIVATE, salehUser.userId))
  //
  //    val stubAli = messagingStub.sendMessage.addHeader(tokenMetadataKey, aliUser.token)
  //    val loadDialogStubAli = messagingStub.loadDialogs.addHeader(tokenMetadataKey, aliUser.token)
  //
  //    val stubHossein = messagingStub.sendMessage().addHeader(tokenMetadataKey, hosseinUser.token)
  //    val loadDialogStubHossein = messagingStub.loadDialogs.addHeader(tokenMetadataKey, hosseinUser.token)
  //    val messageReadStubHossein = messagingStub.messageRead.addHeader(tokenMetadataKey, hosseinUser.token)
  //
  //    val msgToHossein1 = HistoryMessage().withMessage(MessageContent().withTextMessage(TextMessage("salamToHossein1")))
  //    val msgToHossein2 = HistoryMessage().withMessage(MessageContent().withTextMessage(TextMessage("salamToHossein2")))
  //
  //    val messageRsp1 = stubAli.invoke(RequestSendMessage(
  //      hosseinPeer, Random.nextLong(),
  //      Some(msgToHossein1))).futureValue
  //
  //    Thread.sleep(1000)
  //
  //    val messageRsp2 = stubAli.invoke(RequestSendMessage(
  //      hosseinPeer, Random.nextLong(),
  //      Some(msgToHossein2))).futureValue
  //
  //    val dialogAliRsp1 = loadDialogStubAli.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
  //    dialogAliRsp1.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp1.sequenceNr)
  //    dialogAliRsp1.dialogs.head.sortDate shouldBe messageRsp2.date
  //    dialogAliRsp1.dialogs.head.unreadCount shouldBe 0
  //
  //    val dialogHosseinRsp1 = loadDialogStubHossein.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
  //    dialogHosseinRsp1.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp1.sequenceNr)
  //    dialogHosseinRsp1.dialogs.head.sortDate shouldBe messageRsp2.date
  //    dialogHosseinRsp1.dialogs.head.unreadCount shouldBe 2
  //
  //    messageReadStubHossein.invoke(RequestMessageRead(aliPeer, dialogHosseinRsp1.dialogs.head.message.get.sequenceNr)).futureValue
  //
  //    val dialogAliRsp2 = loadDialogStubAli.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
  //    dialogAliRsp2.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp2.sequenceNr)
  //
  //    val dialogHosseinRsp2 = loadDialogStubHossein.invoke(RequestLoadDialogs(Long.MaxValue, 10)).futureValue
  //    dialogHosseinRsp2.dialogs.head.firstUnreadSeq shouldBe Some(messageRsp2.sequenceNr)
  //    dialogHosseinRsp2.dialogs.head.sortDate shouldBe messageRsp2.date
  //    dialogHosseinRsp2.dialogs.head.unreadCount shouldBe 0
  //  }

}
