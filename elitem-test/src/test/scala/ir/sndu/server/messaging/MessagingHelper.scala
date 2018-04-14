package ir.sndu.server.messaging

import ir.sndu.server.{ GrpcBaseSuit, UserInfo }
import ir.sndu.server.peer.ApiOutPeer

import scala.util.Random

trait MessagingHelper {
  self: GrpcBaseSuit =>

  def befrest(peer: ApiOutPeer, msg: ApiMessage)(implicit info: UserInfo): Unit = {
    messagingStub.sendMessage(RequestSendMessage(
      Some(peer),
      Random.nextLong(),
      Some(msg),
      info.token))
  }

  def befrest(peer: ApiOutPeer, msg: String)(implicit info: UserInfo): Unit =
    befrest(peer, ApiMessage().withTextMessage(ApiTextMessage(msg)))

}
