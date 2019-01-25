package im.ghasedak.server.user

import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.api.user.ApiUser
import im.ghasedak.rpc.contact.RequestAddContact
import im.ghasedak.rpc.user.RequestLoadUsers
import im.ghasedak.server.GrpcBaseSuit

class UserServiceSpec extends GrpcBaseSuit {

  behavior of "UserServiceImpl"

  it should "load user without contact" in {
    val ali = createUserWithPhone()
    val sara = createUserWithPhone()

    val aliStubUser1 = userStub.loadUsers.addHeader(tokenMetadataKey, ali.token)

    val rsp = aliStubUser1.invoke(RequestLoadUsers(Seq(sara.userId))).futureValue

    rsp.users should have size 1
    rsp.users.head should have(
      'id(sara.userId),
      'name(sara.name.get),
      'localName(sara.name.get),
      'about(None),
      'contactsRecord(Seq.empty))
  }

  it should "load more than one user without contact" in {
    val users = createUsersWithPhone(5)

    val userStubUser1 = userStub.loadUsers.addHeader(tokenMetadataKey, users.head.token)

    val rsp = userStubUser1.invoke(RequestLoadUsers(users.map(_.userId))).futureValue

    rsp.users should have size users.size
    rsp.users shouldBe users.map(u ⇒ ApiUser(
      id = u.userId,
      name = u.name.get,
      localName = u.name.get,
      about = None,
      contactsRecord = Seq.empty))
  }

  it should "load user with local name" in {
    val users = createUsersWithPhone(6)

    val contactStubUser1 = contactStub.addContact.addHeader(tokenMetadataKey, users.head.token)
    val contactStubUser2 = contactStub.addContact.addHeader(tokenMetadataKey, users(1).token)
    val userStubUser1 = userStub.loadUsers.addHeader(tokenMetadataKey, users.head.token)

    val userForContact = users.slice(2, 5)

    //Adds 3 user to contact of user1 and user2
    val requests = userForContact.zipWithIndex.map {
      case (user, index) ⇒
        RequestAddContact(
          localName = Some(s"user-0$index"),
          contactRecord = Some(ApiContactRecord().withPhoneNumber(user.phoneNumber.get)))
    }

    requests foreach (contactStubUser1.invoke(_).futureValue)
    requests foreach (contactStubUser2.invoke(_).futureValue)

    val rsp = userStubUser1.invoke(RequestLoadUsers(userForContact.map(_.userId))).futureValue

    val userForContactApiUsers = userForContact.zipWithIndex.map {
      case (user, index) ⇒ ApiUser(
        user.userId,
        user.name.get,
        s"user-0$index",
        None,
        Seq(ApiContactRecord().withPhoneNumber(user.phoneNumber.get)))
    }

    rsp.users should have size userForContact.size
    rsp.users shouldBe userForContactApiUsers
  }

}
