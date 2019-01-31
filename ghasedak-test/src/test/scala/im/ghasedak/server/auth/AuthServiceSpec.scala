package im.ghasedak.server.auth

import java.time.temporal.ChronoUnit
import java.time.{ LocalDateTime, ZoneOffset }
import java.util.UUID

import im.ghasedak.rpc.auth._
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.rpc.test.RequestCheckAuth
import im.ghasedak.server.GrpcBaseSuit
import im.ghasedak.server.repo.auth._
import io.grpc.Status.Code
import io.grpc.StatusRuntimeException

import scala.util.Random

class AuthServiceSpec extends GrpcBaseSuit {

  behavior of "AuthServiceImpl"

  it should "start phone auth and get transaction hash" in startPhoneAuth

  it should "get same transaction hash in twice start phone auth" in sameTransactionHash

  it should "get different transaction hash in twice start phone auth after expiration" in expireTransactionHash

  it should "get invalid phone number in start phone auth" in invalidPhoneNumber

  it should "return error in validate code with invalid transaction" in invalidTransaction

  it should "validate auth code" in testValidateCode

  it should "return invalid auth code in validate phone auth code" in invalidAuthCode

  it should "return auth code expired in validate phone auth code" in authCodeExpired

  it should "successfully sign up" in signUp

  it should "sign up and after that sign in" in signUpAndSignIn

  it should "authorized client after sign up" in authorizedAfterSignUp

  it should "return different transaction hash after validate" in differentTransactionHashAfterValidate

  it should "sign out and cant send request again" in signOut

  it should "successfully login with test phone number" in testPhoneNumber

  it should "invalidate transaction hash after more attempts" in moreAttemptsForInvalidate

  def startPhoneAuth(): Unit = {
    val request = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response = authStub.startPhoneAuth.invoke(request).futureValue
    response.transactionHash should not be empty
    db.run(GateAuthCodeRepo.find(response.transactionHash)).futureValue should not be None
  }

  def sameTransactionHash(): Unit = {
    val request = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request).futureValue
    val response2 = authStub.startPhoneAuth.invoke(request).futureValue
    response1.transactionHash shouldEqual response2.transactionHash
  }

  def expireTransactionHash(): Unit = {
    val request = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request).futureValue
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val oldDate = now.minusMinutes(20)
    oldDate.until(now, ChronoUnit.MINUTES) shouldEqual 20
    db.run(AuthTransactionRepo.updateCreateAt(response1.transactionHash, oldDate)).futureValue
    val response2 = authStub.startPhoneAuth.invoke(request).futureValue
    response1.transactionHash should not equal response2.transactionHash
  }

  def invalidPhoneNumber(): Unit = {
    val request = RequestStartPhoneAuth(2, officialApiKeys.head.apiKey)
    authStub.startPhoneAuth.invoke(request).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "INVALID_PHONE_NUMBER"
    }
  }

  def invalidTransaction(): Unit = {
    val request = RequestValidateCode(UUID.randomUUID().toString, "12345")
    authStub.validateCode.invoke(request).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "AUTH_CODE_EXPIRED"
    }
  }

  def testValidateCode(): Unit = {
    val request1 = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    val response2 = authStub.validateCode.invoke(request2).futureValue
    response2.isRegistered shouldEqual false
    response2.apiAuth shouldEqual None
  }

  def invalidAuthCode(): Unit = {
    val request1 = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, "12345")
    authStub.validateCode.invoke(request2).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "INVALID_AUTH_CODE"
    }
  }

  def authCodeExpired(): Unit = {
    val request1 = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val oldDate = now.minusMinutes(20)
    oldDate.until(now, ChronoUnit.MINUTES) shouldEqual 20
    db.run(AuthTransactionRepo.updateCreateAt(response1.transactionHash, oldDate)).futureValue
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    authStub.validateCode.invoke(request2).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "AUTH_CODE_EXPIRED"
    }
  }

  def signUp(): Unit = {
    val request1 = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    val response2 = authStub.validateCode.invoke(request2).futureValue
    response2.isRegistered shouldEqual false
    response2.apiAuth shouldEqual None
    val request3 = RequestSignUp(
      response1.transactionHash,
      Random.alphanumeric.take(20).mkString)
    val response3 = authStub.signUp.invoke(request3).futureValue
    response3.isRegistered shouldEqual true
    response3.apiAuth should not be None
  }

  def signUpAndSignIn(): Unit = {
    val request1 = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val codeGate1 = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate1.get.codeHash)
    val response2 = authStub.validateCode.invoke(request2).futureValue
    response2.isRegistered shouldEqual false
    response2.apiAuth shouldEqual None
    val request3 = RequestSignUp(
      response1.transactionHash,
      Random.alphanumeric.take(20).mkString)
    val response3 = authStub.signUp.invoke(request3).futureValue
    response3.isRegistered shouldEqual true
    response3.apiAuth should not be None
    val request4 = request1
    val response4 = authStub.startPhoneAuth.invoke(request4).futureValue
    val codeGate2 = db.run(GateAuthCodeRepo.find(response4.transactionHash)).futureValue
    val request5 = RequestValidateCode(response4.transactionHash, codeGate2.get.codeHash)
    val response5 = authStub.validateCode.invoke(request5).futureValue
    response5.isRegistered shouldEqual true
    response5.apiAuth should not be None
    response5.apiAuth.get.user.get shouldEqual response3.apiAuth.get.user.get
    response5.apiAuth.get.token should not equal response3.apiAuth.get.token
  }

  def authorizedAfterSignUp(): Unit = {
    val user = createUserWithPhone()
    val response = testStub.checkAuth.addHeader(tokenMetadataKey, user.token).invoke(RequestCheckAuth()).futureValue
    response shouldEqual ResponseVoid()
  }

  def differentTransactionHashAfterValidate(): Unit = {
    val request1 = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    authStub.validateCode.invoke(request2).futureValue
    val response3 = authStub.startPhoneAuth.invoke(request1).futureValue
    response3.transactionHash should not equal response1.transactionHash
  }

  def signOut(): Unit = {
    val user = createUserWithPhone()
    authStub.signOut.addHeader(tokenMetadataKey, user.token).invoke(RequestSignOut()).futureValue
    testStub.checkAuth.addHeader(tokenMetadataKey, user.token).invoke(RequestCheckAuth()).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.UNAUTHENTICATED
        ex.getStatus.getDescription shouldEqual "INVALID_TOKEN"
    }
  }

  def testPhoneNumber(): Unit = {
    val (phoneNumber, code) = generateTestPhoneNumber()
    val request1 = RequestStartPhoneAuth(phoneNumber, officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request1).futureValue
    val request2 = RequestValidateCode(response1.transactionHash, code)
    val response2 = authStub.validateCode.invoke(request2).futureValue
    response2.isRegistered shouldEqual false
    response2.apiAuth shouldEqual None
    val request3 = RequestSignUp(
      response1.transactionHash,
      Random.alphanumeric.take(20).mkString)
    val response3 = authStub.signUp.invoke(request3).futureValue
    response3.isRegistered shouldEqual true
    response3.apiAuth should not be None
  }

  def moreAttemptsForInvalidate(): Unit = {
    val request = RequestStartPhoneAuth(generatePhoneNumber(), officialApiKeys.head.apiKey)
    val response1 = authStub.startPhoneAuth.invoke(request).futureValue
    val codeGate = db.run(GateAuthCodeRepo.find(response1.transactionHash)).futureValue
    for (_ ← 1 to 4) {
      val request2 = RequestValidateCode(response1.transactionHash, "11111")
      authStub.validateCode.invoke(request2).failed.futureValue match {
        case ex: StatusRuntimeException ⇒
          ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
          ex.getStatus.getDescription shouldEqual "INVALID_AUTH_CODE"
      }
    }
    val request2 = RequestValidateCode(response1.transactionHash, codeGate.get.codeHash)
    authStub.validateCode.invoke(request2).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "AUTH_CODE_EXPIRED"
    }
    val response2 = authStub.startPhoneAuth.invoke(request).futureValue
    response1.transactionHash should not equal response2.transactionHash
  }

}