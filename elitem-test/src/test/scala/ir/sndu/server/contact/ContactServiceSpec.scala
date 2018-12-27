package ir.sndu.server.contact

import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.rpc.contact.{ RequestAddContact, RequestGetContacts, RequestRemoveContact }
import io.grpc.Status.Code
import io.grpc.StatusRuntimeException
import ir.sndu.server.GrpcBaseSuit
import ir.sndu.server.rpc.Constant

import scala.util.{ Failure, Random, Try }

class ContactServiceSpec extends GrpcBaseSuit {

  behavior of "ContactServiceImpl"

  it should "add contact with phone number and get contact user id" in addContactWithPhoneNumber

  it should "get invalid contact record error" in invalidContactError

  it should "get cant add self error" in cantAddSelfError

  it should "get contact already exist error" in contactAlreadyExist

  it should "get user not found error" in userNotFound

  it should "get all contacts user id" in getAllContacts

  it should "remove his contact" in removeContact

  def addContactWithPhoneNumber(): Unit = {
    val user1 = createPhoneNumberUser()
    val user2 = createPhoneNumberUser()
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
    val request = RequestAddContact(
      localName = Random.alphanumeric.take(20).mkString,
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber)))
    val response = stub.addContact(request)
    response.contactUserId shouldEqual user2.userId
  }

  def invalidContactError(): Unit = {
    val user1 = createPhoneNumberUser()
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
    val request1 = RequestAddContact(Random.alphanumeric.take(20).mkString)
    Try(stub.addContact(request1)) match {
      case Failure(ex: StatusRuntimeException) ⇒
        ex.getStatus.getCode shouldEqual Code.INTERNAL
        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "INVALID_CONTACT_RECORD"
    }
    val request2 = RequestAddContact(Random.alphanumeric.take(20).mkString, Some(ApiContactRecord()))
    Try(stub.addContact(request2)) match {
      case Failure(ex: StatusRuntimeException) ⇒
        ex.getStatus.getCode shouldEqual Code.INTERNAL
        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "INVALID_CONTACT_RECORD"
    }
  }

  def cantAddSelfError(): Unit = {
    val user = createPhoneNumberUser()
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user.token))
    val request = RequestAddContact(
      Random.alphanumeric.take(20).mkString,
      Some(ApiContactRecord().withPhoneNumber(user.phoneNumber)))
    Try(stub.addContact(request)) match {
      case Failure(ex: StatusRuntimeException) ⇒
        ex.getStatus.getCode shouldEqual Code.INTERNAL
        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "CANT_ADD_SELF"
    }
  }

  def contactAlreadyExist(): Unit = {
    val user1 = createPhoneNumberUser()
    val user2 = createPhoneNumberUser()
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
    val request = RequestAddContact(
      localName = Random.alphanumeric.take(20).mkString,
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber)))
    stub.addContact(request)
    Try(stub.addContact(request)) match {
      case Failure(ex: StatusRuntimeException) ⇒
        ex.getStatus.getCode shouldEqual Code.INTERNAL
        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "CONTACT_ALREADY_EXISTS"
    }
  }

  def userNotFound(): Unit = {
    val user = createPhoneNumberUser()
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user.token))
    val request = RequestAddContact(
      Random.alphanumeric.take(20).mkString,
      Some(ApiContactRecord().withPhoneNumber(generatePhoneNumber())))
    Try(stub.addContact(request)) match {
      case Failure(ex: StatusRuntimeException) ⇒
        ex.getStatus.getCode shouldEqual Code.NOT_FOUND
        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "USER_NOT_FOUND"
    }
  }

  def getAllContacts(): Unit = {
    val n = 10
    val user = createPhoneNumberUser()
    val contacts = Seq.fill(n)(createPhoneNumberUser())
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user.token))
    contacts foreach { contact ⇒
      val request = RequestAddContact(
        localName = Random.alphanumeric.take(20).mkString,
        Some(ApiContactRecord().withPhoneNumber(contact.phoneNumber)))
      stub.addContact(request)
    }
    val request = RequestGetContacts()
    val response = stub.getContacts(request)
    contacts foreach { contact ⇒
      response.contactsUserId.contains(contact.userId) shouldEqual true
    }
  }

  def removeContact(): Unit = {
    val user1 = createPhoneNumberUser()
    val user2 = createPhoneNumberUser()
    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
    val request = RequestAddContact(
      localName = Random.alphanumeric.take(20).mkString,
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber)))
    stub.addContact(request)
    stub.getContacts(RequestGetContacts()).contactsUserId.contains(user2.userId) shouldEqual true
    stub.removeContact(RequestRemoveContact(user2.userId))
    stub.getContacts(RequestGetContacts()).contactsUserId.contains(user2.userId) shouldEqual false
  }

}
