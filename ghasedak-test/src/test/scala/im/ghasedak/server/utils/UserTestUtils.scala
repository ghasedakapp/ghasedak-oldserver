package im.ghasedak.server.utils

import im.ghasedak.rpc.auth._
import im.ghasedak.server.GrpcBaseSuit
import im.ghasedak.server.repo.auth.GateAuthCodeRepo
import im.ghasedak.server.rpc.Constant
import im.ghasedak.server.utils.UserTestUtils.TestClientData
import io.grpc._

import scala.util.Random

object UserTestUtils {

  case class TestClientData(userId: Int, token: String, phoneNumber: Option[Long] = None, name: Option[String] = None)

}

trait UserTestUtils {
  this: GrpcBaseSuit ⇒

  protected val tokenMetadataKey = Constant.tokenMetadataKey

  // todo: remove this in future
  protected def clientTokenInterceptor(token: String): ClientInterceptor = {
    new ClientInterceptor {
      override def interceptCall[ReqT, RespT](method: MethodDescriptor[ReqT, RespT], callOptions: CallOptions, next: Channel): ClientCall[ReqT, RespT] = {
        new ClientInterceptors.CheckedForwardingClientCall[ReqT, RespT](next.newCall(method, callOptions)) {
          @throws[StatusException]
          override protected def checkedStart(responseListener: ClientCall.Listener[RespT], headers: Metadata): Unit = {
            headers.put(Constant.TOKEN_METADATA_KEY, token)
            delegate.start(responseListener, headers)
          }
        }
      }
    }
  }

  protected def generatePhoneNumber(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  protected def generateTestPhoneNumber(): (Long, String) = {
    val prefix = config.getString("module.auth.test-phone-number.prefix")
    val strPhone = ((prefix + "0000000").toLong + scala.util.Random.nextInt(999999)).toString
    val code = "1" + strPhone(4).toString * 2 + strPhone(5).toString * 2
    (strPhone.toLong, code)
  }

  protected def createUserWithPhone(): TestUser = {
    val phone = generatePhoneNumber()
    val request1 = RequestStartPhoneAuth(phone, officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    authStub.validateCode.invoke(request2).futureValue
    val name = Random.alphanumeric.take(20).mkString
    val request3 = RequestSignUp(
      response1.transactionHash,
      name)
    val response3 = authStub.signUp.invoke(request3).futureValue
    TestClientData(
      response3.getApiAuth.getUser.id,
      response3.getApiAuth.token,
      Some(response3.getApiAuth.getUser.contactsRecord.head.getPhoneNumber),
      Some(name))
  }

  protected def createUsersWithPhone(num: Int): Seq[TestUser] =
    1 to num map (_ ⇒ createUserWithPhone())

}
