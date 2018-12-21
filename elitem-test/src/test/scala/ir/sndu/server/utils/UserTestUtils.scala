package ir.sndu.server.utils

import im.ghasedak.rpc.auth.{ RequestSignUp, RequestStartPhoneAuth, RequestValidateCode }
import io.grpc._
import ir.sndu.persist.repo.auth.GateAuthCodeRepo
import ir.sndu.server.GrpcBaseSuit
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper
import ir.sndu.server.utils.UserTestUtils.PhoneNumberTestClientData

import scala.util.Random

object UserTestUtils {

  case class PhoneNumberTestClientData(userId: Int, token: String, phoneNumber: Long)

}

trait UserTestUtils {
  this: GrpcBaseSuit ⇒

  protected def clientTokenInterceptor(token: String): ClientInterceptor = {
    new ClientInterceptor {
      override def interceptCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT], callOptions: CallOptions, next: Channel): ClientCall[ReqT, RespT] = {
        new ClientInterceptors.CheckedForwardingClientCall[ReqT, RespT](next.newCall(method, callOptions)) {
          @throws[StatusException]
          override protected def checkedStart(responseListener: ClientCall.Listener[RespT], headers: Metadata): Unit = {
            headers.put(AuthTokenHelper.TOKEN_METADATA_KEY, token)
            delegate.start(responseListener, headers)
          }
        }
      }
    }
  }

  protected def generatePhoneNumber(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  protected def createPhoneNumberUser(): PhoneNumberTestClientData = {
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
    PhoneNumberTestClientData(
      response3.getApiAuth.getUser.id,
      response3.getApiAuth.token,
      response3.getApiAuth.getUser.contactsRecoed.head.getPhoneNumber)
  }

}
