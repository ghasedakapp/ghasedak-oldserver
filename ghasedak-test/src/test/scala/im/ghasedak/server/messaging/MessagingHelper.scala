package im.ghasedak.server.messaging

import im.ghasedak.api.chat.{ Chat, ChatType }
import im.ghasedak.api.messaging.MessageContent
import im.ghasedak.rpc.chat.RequestCreateChat
import im.ghasedak.rpc.messaging.{ RequestLoadHistory, RequestSendMessage, ResponseLoadHistory }
import im.ghasedak.server.GrpcBaseSuit

import scala.util.Random

trait MessagingHelper {
  this: GrpcBaseSuit ⇒

  def sendPrivateMessage(token: String, message: MessageContent, peerId: Int, chat: Option[Chat] = None): Chat = {
    val chatStubAli = createChatStub(token)
    val sendStubAli = sendMessageStub(token)
    val ch = chat.getOrElse {
      chatStubAli.invoke(RequestCreateChat(
        Random.nextLong(), ChatType.CHATTYPE_PRIVATE.value, "title" + Random.nextString(5), Seq(peerId)))
        .futureValue
        .chat.get
    }

    sendStubAli.invoke(RequestSendMessage(
      ch.id, Random.nextLong(),
      Some(message))).futureValue

    ch
  }

  def loadHistory(token: String, chat: Chat): ResponseLoadHistory = {
    val stub = loadHistoryStub(token)

    stub.invoke(RequestLoadHistory(
      chat.id,
      sequenceNr = Int.MaxValue,
      limit = 10)).futureValue

  }

}
