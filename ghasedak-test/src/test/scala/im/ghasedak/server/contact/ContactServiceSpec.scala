package im.ghasedak.server.contact

import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.rpc.contact.{ RequestAddContact, RequestGetContacts, RequestRemoveContact }
import im.ghasedak.rpc.user.RequestLoadUsers
import im.ghasedak.server.GrpcBaseSuit
import im.ghasedak.server.repo.user.UserRepo
import io.grpc.Status.Code
import io.grpc.StatusRuntimeException

import scala.util.Random

class ContactServiceSpec extends GrpcBaseSuit {

  behavior of "ContactServiceImpl"

  it should "add contact with phone number and get contact user id" in addContactWithPhoneNumber

  it should "get invalid contact record error" in invalidContactError

  it should "get cant add self error" in cantAddSelfError

  it should "get contact already exist error" in contactAlreadyExist

  it should "get user not found error" in userNotFound

  it should "get all contacts user id" in getAllContacts

  it should "remove his contact" in removeContact

  it should "add contact without local name" in addWithoutLocalName

  def addContactWithPhoneNumber(): Unit = {
    val user1 = createUserWithPhone()
    val user2 = createUserWithPhone()
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user1.token)
    val request = RequestAddContact(
      localName = Some(Random.alphanumeric.take(20).mkString),
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
    val response = stub.invoke(request).futureValue
    response.contactUserId shouldEqual user2.userId
  }

  def invalidContactError(): Unit = {
    val user1 = createUserWithPhone()
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user1.token)
    val request1 = RequestAddContact(Some(Random.alphanumeric.take(20).mkString))
    stub.invoke(request1).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "INVALID_CONTACT_RECORD"
    }
    val request2 = RequestAddContact(Some(Random.alphanumeric.take(20).mkString), Some(ApiContactRecord()))
    stub.invoke(request2).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
        ex.getStatus.getDescription shouldEqual "INVALID_CONTACT_RECORD"
    }
  }

  def cantAddSelfError(): Unit = {
    val user = createUserWithPhone()
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user.token)
    val request = RequestAddContact(
      Some(Random.alphanumeric.take(20).mkString),
      Some(ApiContactRecord().withPhoneNumber(user.phoneNumber.get)))
    stub.invoke(request).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.FAILED_PRECONDITION
        ex.getStatus.getDescription shouldEqual "CANT_ADD_SELF"
    }
  }

  def contactAlreadyExist(): Unit = {
    val user1 = createUserWithPhone()
    val user2 = createUserWithPhone()
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user1.token)
    val request = RequestAddContact(
      localName = Some(Random.alphanumeric.take(20).mkString),
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
    stub.invoke(request).futureValue
    stub.invoke(request).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.FAILED_PRECONDITION
        ex.getStatus.getDescription shouldEqual "CONTACT_ALREADY_EXISTS"
    }
  }

  def userNotFound(): Unit = {
    val user = createUserWithPhone()
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user.token)
    val request = RequestAddContact(
      Some(Random.alphanumeric.take(20).mkString),
      Some(ApiContactRecord().withPhoneNumber(generatePhoneNumber())))
    stub.invoke(request).failed.futureValue match {
      case ex: StatusRuntimeException ⇒
        ex.getStatus.getCode shouldEqual Code.NOT_FOUND
        ex.getStatus.getDescription shouldEqual "USER_NOT_FOUND"
    }
  }

  def getAllContacts(): Unit = {
    val n = 10
    val user = createUserWithPhone()
    val contacts = Seq.fill(n)(createUserWithPhone())
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user.token)
    val getContactStub = contactStub.getContacts().addHeader(tokenMetadataKey, user.token)
    contacts foreach { contact ⇒
      val request = RequestAddContact(
        localName = Some(Random.alphanumeric.take(20).mkString),
        Some(ApiContactRecord().withPhoneNumber(contact.phoneNumber.get)))
      stub.invoke(request).futureValue
    }
    val request = RequestGetContacts()
    val response = getContactStub.invoke(request).futureValue
    response.contactUserIds should contain allElementsOf contacts.map(_.userId)
  }

  def removeContact(): Unit = {
    val user1 = createUserWithPhone()
    val user2 = createUserWithPhone()
    val addContactStub = contactStub.addContact.addHeader(tokenMetadataKey, user1.token)
    val getContactStub = contactStub.getContacts.addHeader(tokenMetadataKey, user1.token)
    val removeContactStub = contactStub.removeContact().addHeader(tokenMetadataKey, user1.token)
    val request = RequestAddContact(
      localName = Some(Random.alphanumeric.take(20).mkString),
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
    addContactStub.invoke(request).futureValue
    getContactStub.invoke(RequestGetContacts()).futureValue.contactUserIds.contains(user2.userId) shouldEqual true
    removeContactStub.invoke(RequestRemoveContact(user2.userId)).futureValue
    getContactStub.invoke(RequestGetContacts()).futureValue.contactUserIds.contains(user2.userId) shouldEqual false
  }

  def addWithoutLocalName(): Unit = {
    val user1 = createUserWithPhone()
    val user2 = createUserWithPhone()
    val stub = contactStub.addContact.addHeader(tokenMetadataKey, user1.token)
    val uStub = userStub.loadUsers.addHeader(tokenMetadataKey, user1.token)
    val request = RequestAddContact(
      localName = None,
      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
    val response = stub.invoke(request).futureValue
    response.contactUserId shouldEqual user2.userId

    val name = db.run(UserRepo.find(user2.userId)).futureValue.get.name
    uStub.invoke(RequestLoadUsers(Seq(response.contactUserId))).futureValue.users.head.localName shouldEqual name
  }

}
