package ir.sndu.server.messaging

import ir.sndu.api.peer.{ ApiPeer, ApiPeerType }
import ir.sndu.rpc.messaging.RequestSendMessage
import ir.sndu.server.GrpcBaseSuit

class MessagingServiceSpec extends GrpcBaseSuit {

  behavior of "MessagingServiceImpl"

  it should "send message with sequence number" in {
    val user1 = createUser()
    val user2 = createUser()
    val stub = messagingStub.withInterceptors(clientTokenInterceptor(user1.token))
    stub.sendMessage(RequestSendMessage(Some(ApiPeer(ApiPeerType.PRIVATE, user2.userId))))
  }

}
