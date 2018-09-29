package ir.sndu.server.messaging

import ir.sndu.server.message.{ ApiMessage, ApiTextMessage }
import ir.sndu.server.peer.ApiOutPeer
import ir.sndu.server.{ GrpcBaseSuit, ClientData }

import scala.util.Random

trait MessagingHelper {
  self: GrpcBaseSuit ⇒

  def befrest(peer: ApiOutPeer, msg: ApiMessage)(implicit info: ClientData): Unit = {
    messagingStub.sendMessage(RequestSendMessage(
      Some(peer),
      Random.nextLong(),
      Some(msg),
      info.token))
  }

  def befrest(peer: ApiOutPeer, msg: String)(implicit info: ClientData): Unit =
    befrest(peer, ApiMessage().withTextMessage(ApiTextMessage(msg)))

}
