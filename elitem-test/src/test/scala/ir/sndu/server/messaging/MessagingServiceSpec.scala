package ir.sndu.server.messaging

import io.grpc._
import ir.sndu.api.peer.{ ApiPeer, ApiPeerType }
import ir.sndu.persist.repo.auth.GateAuthCodeRepo
import ir.sndu.rpc.auth.{ RequestSignUp, RequestStartPhoneAuth, RequestValidateCode }
import ir.sndu.rpc.messaging.{ MessagingServiceGrpc, RequestSendMessage }
import ir.sndu.server.GrpcBaseSuit
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper

import scala.util.Random

class MessagingServiceSpec extends GrpcBaseSuit {

  behavior of "MessagingServiceImpl"

  it should "send message with sequence number" in {
    val user1 = createUser()
    val user2 = createUser()
    MessagingServiceGrpc.blockingStub(channel).withInterceptors(metadataInterceptor(user1.token))
      .sendMessage(RequestSendMessage(
        Some(ApiPeer(ApiPeerType.PRIVATE, user2.userId))))

  }

  case class ClientData(userId: Int, token: String, phone: Long)

  import io.grpc.CallOptions
  import io.grpc.ClientCall
  import io.grpc.ClientInterceptor
  import io.grpc.ClientInterceptors
  import io.grpc.Metadata
  import io.grpc.StatusException

  private def metadataInterceptor(token: String) = {
    val interceptor = new ClientInterceptor() {
      def interceptCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT], callOptions: CallOptions, next: Channel): ClientCall[ReqT, RespT] = new ClientInterceptors.CheckedForwardingClientCall[ReqT, RespT](next.newCall(method, callOptions)) {
        @throws[StatusException]
        override protected def checkedStart(responseListener: ClientCall.Listener[RespT], headers: Metadata): Unit = {
          val key = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER)
          headers.put(key, token)
          delegate.start(responseListener, headers)
        }
      }
    }
    interceptor
  }

  protected def createUser(): ClientData = {
    val phone = generatePhoneNumber()
    val request1 = RequestStartPhoneAuth(
      phone, 1,
      "4b654ds5b4654sd65b44s6d5b46s5d4b",
      Random.nextString(10), "device info")
    val response1 = authStub.startPhoneAuth(request1)
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    authStub.validateCode(request2)
    val request3 = RequestSignUp(
      response1.transactionHash,
      Random.alphanumeric.take(20).mkString)
    val response3 = authStub.signUp(request3)
    ClientData(
      response3.getApiAuth.getUser.id,
      response3.getApiAuth.token,
      response3.getApiAuth.getUser.phoneNumber.get)
  }
}
