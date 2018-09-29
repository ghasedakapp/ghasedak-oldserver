package ir.sndu.server.messaging

import ir.sndu.server.{ GrpcBaseSuit, ClientData }
import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.message.{ ApiMessage, ApiTextMessage }
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }

class DialogSpec extends GrpcBaseSuit
  with AuthHelper
  with MessagingHelper {

  "load dialog" should "" in loadDialog

  def loadDialog(): Unit = {
    implicit val userInfo1: ClientData = createUser()
    val ClientData(user1, token1, _) = userInfo1
    val ClientData(user2, token2, _) = createUser()
    val ClientData(user3, token3, _) = createUser()

    val outPeer2 = ApiOutPeer(ApiPeerType.Private, user2.id)
    val outPeer3 = ApiOutPeer(ApiPeerType.Private, user3.id)

    val msg1 = ApiMessage().withTextMessage(ApiTextMessage("salam1"))
    val msg2 = ApiMessage().withTextMessage(ApiTextMessage("salam2"))
    val msg3 = ApiMessage().withTextMessage(ApiTextMessage("salam3"))

    befrest(outPeer2, msg1)
    Thread.sleep(100)
    befrest(outPeer3, msg2)
    Thread.sleep(100)
    befrest(outPeer3, msg3)

    val rspUser1 = messagingStub.loadDialogs(RequestLoadDialogs(10, token1))
    rspUser1.dialogs.size shouldBe 2
    rspUser1.dialogs.map(_.message.get) shouldBe Seq(msg3, msg1)

    val rspUser2 = messagingStub.loadDialogs(RequestLoadDialogs(10, token2))
    rspUser2.dialogs.size shouldBe 1

    val rspUser3 = messagingStub.loadDialogs(RequestLoadDialogs(10, token3))
    rspUser3.dialogs.size shouldBe 1
    rspUser3.dialogs.map(_.message.get) shouldBe Seq(msg3)

  }
}
