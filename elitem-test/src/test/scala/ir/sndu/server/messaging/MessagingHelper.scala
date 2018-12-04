package ir.sndu.server.messaging

import ir.sndu.api.message._
import ir.sndu.api.peer.ApiOutPeer
import ir.sndu.rpc.messaging.RequestSendMessage
import ir.sndu.server.{ ClientData, GrpcBaseSuit }

import scala.util.Random

trait MessagingHelper {
  self: GrpcBaseSuit ⇒

  def befrest(peer: ApiOutPeer, msg: ApiMessage)(implicit info: ClientData): Unit = {
    messagingStub.sendMessage(RequestSendMessage(
      Some(peer),
      Random.nextLong(),
      Some(msg)))
  }

  def befrest(peer: ApiOutPeer, msg: String)(implicit info: ClientData): Unit =
    befrest(peer, ApiMessage().withTextMessage(ApiTextMessage(msg)))

}
