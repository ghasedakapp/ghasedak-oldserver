//package im.ghasedak.server.contact
//
//import im.ghasedak.api.contact.ApiContactRecord
//import im.ghasedak.rpc.contact.{ RequestAddContact, RequestGetContacts, RequestRemoveContact }
//import im.ghasedak.rpc.user.RequestLoadUsers
//import io.grpc.Status.Code
//import io.grpc.StatusRuntimeException
//import im.ghasedak.server.repo.user.UserRepo
//import im.ghasedak.server.GrpcBaseSuit
//import im.ghasedak.server.rpc.Constant
//
//import scala.util.{ Failure, Random, Try }
//
//class ContactServiceSpec extends GrpcBaseSuit {
//
//  behavior of "ContactServiceImpl"
//
//  it should "add contact with phone number and get contact user id" in addContactWithPhoneNumber
//
//  it should "get invalid contact record error" in invalidContactError
//
//  it should "get cant add self error" in cantAddSelfError
//
//  it should "get contact already exist error" in contactAlreadyExist
//
//  it should "get user not found error" in userNotFound
//
//  it should "get all contacts user id" in getAllContacts
//
//  it should "remove his contact" in removeContact
//
//  it should "add contact without local name" in addWithoutLocalName
//
//  def addContactWithPhoneNumber(): Unit = {
//    val user1 = createUserWithPhone()
//    val user2 = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
//    val request = RequestAddContact(
//      localName = Some(Random.alphanumeric.take(20).mkString),
//      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
//    val response = stub.addContact(request)
//    response.contactUserId shouldEqual user2.userId
//  }
//
//  def invalidContactError(): Unit = {
//    val user1 = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
//    val request1 = RequestAddContact(Some(Random.alphanumeric.take(20).mkString))
//    Try(stub.addContact(request1)) match {
//      case Failure(ex: StatusRuntimeException) ⇒
//        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
//        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "INVALID_CONTACT_RECORD"
//    }
//    val request2 = RequestAddContact(Some(Random.alphanumeric.take(20).mkString), Some(ApiContactRecord()))
//    Try(stub.addContact(request2)) match {
//      case Failure(ex: StatusRuntimeException) ⇒
//        ex.getStatus.getCode shouldEqual Code.INVALID_ARGUMENT
//        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "INVALID_CONTACT_RECORD"
//    }
//  }
//
//  def cantAddSelfError(): Unit = {
//    val user = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user.token))
//    val request = RequestAddContact(
//      Some(Random.alphanumeric.take(20).mkString),
//      Some(ApiContactRecord().withPhoneNumber(user.phoneNumber.get)))
//    Try(stub.addContact(request)) match {
//      case Failure(ex: StatusRuntimeException) ⇒
//        ex.getStatus.getCode shouldEqual Code.FAILED_PRECONDITION
//        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "CANT_ADD_SELF"
//    }
//  }
//
//  def contactAlreadyExist(): Unit = {
//    val user1 = createUserWithPhone()
//    val user2 = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
//    val request = RequestAddContact(
//      localName = Some(Random.alphanumeric.take(20).mkString),
//      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
//    stub.addContact(request)
//    Try(stub.addContact(request)) match {
//      case Failure(ex: StatusRuntimeException) ⇒
//        ex.getStatus.getCode shouldEqual Code.FAILED_PRECONDITION
//        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "CONTACT_ALREADY_EXISTS"
//    }
//  }
//
//  def userNotFound(): Unit = {
//    val user = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user.token))
//    val request = RequestAddContact(
//      Some(Random.alphanumeric.take(20).mkString),
//      Some(ApiContactRecord().withPhoneNumber(generatePhoneNumber())))
//    Try(stub.addContact(request)) match {
//      case Failure(ex: StatusRuntimeException) ⇒
//        ex.getStatus.getCode shouldEqual Code.NOT_FOUND
//        ex.getTrailers.get(Constant.TAG_METADATA_KEY) shouldEqual "USER_NOT_FOUND"
//    }
//  }
//
//  def getAllContacts(): Unit = {
//    val n = 10
//    val user = createUserWithPhone()
//    val contacts = Seq.fill(n)(createUserWithPhone())
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user.token))
//    contacts foreach { contact ⇒
//      val request = RequestAddContact(
//        localName = Some(Random.alphanumeric.take(20).mkString),
//        Some(ApiContactRecord().withPhoneNumber(contact.phoneNumber.get)))
//      stub.addContact(request)
//    }
//    val request = RequestGetContacts()
//    val response = stub.getContacts(request)
//    response.contactUserIds should contain allElementsOf contacts.map(_.userId)
//  }
//
//  def removeContact(): Unit = {
//    val user1 = createUserWithPhone()
//    val user2 = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
//    val request = RequestAddContact(
//      localName = Some(Random.alphanumeric.take(20).mkString),
//      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
//    stub.addContact(request)
//    stub.getContacts(RequestGetContacts()).contactUserIds.contains(user2.userId) shouldEqual true
//    stub.removeContact(RequestRemoveContact(user2.userId))
//    stub.getContacts(RequestGetContacts()).contactUserIds.contains(user2.userId) shouldEqual false
//  }
//
//  def addWithoutLocalName(): Unit = {
//    val user1 = createUserWithPhone()
//    val user2 = createUserWithPhone()
//    val stub = contactStub.withInterceptors(clientTokenInterceptor(user1.token))
//    val uStub = userStub.withInterceptors(clientTokenInterceptor(user1.token))
//    val request = RequestAddContact(
//      localName = None,
//      Some(ApiContactRecord().withPhoneNumber(user2.phoneNumber.get)))
//    val response = stub.addContact(request)
//    response.contactUserId shouldEqual user2.userId
//
//    val name = db.run(UserRepo.find(user2.userId)).futureValue.get.name
//    uStub.loadUsers(RequestLoadUsers(Seq(response.contactUserId))).users.head.localName shouldEqual name
//  }
//
//}
