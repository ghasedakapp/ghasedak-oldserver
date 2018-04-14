package ir.sndu.server.messaging

import ir.sndu.server.{ GrpcBaseSuit, UserInfo }
import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }

class DialogSpec extends GrpcBaseSuit
  with AuthHelper
  with MessagingHelper {

  "load dialog" should "" in loadDialog

  def loadDialog(): Unit = {
    implicit val userInfo1: UserInfo = createUser()
    val UserInfo(user1, token1, _) = userInfo1
    val UserInfo(user2, token2, _) = createUser()

    val outPeer2 = ApiOutPeer(ApiPeerType.Private, user2.id)

    befrest(outPeer2, "test")

    messagingStub.loadDialogs(RequestLoadDialogs(10, token1))
  }
}
