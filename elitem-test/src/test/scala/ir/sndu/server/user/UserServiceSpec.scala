package ir.sndu.server.user

import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.api.user.ApiUser
import im.ghasedak.rpc.contact.RequestAddContact
import im.ghasedak.rpc.user.RequestLoadUsers
import ir.sndu.server.GrpcBaseSuit

class UserServiceSpec extends GrpcBaseSuit {

  behavior of "UserServiceImpl"

  ignore should "load user" in {
    val ali = createUserWithPhone()
    val sara = createUserWithPhone()

    val aliStubUser1 = userStub.withInterceptors(clientTokenInterceptor(ali.token))

    val rsp = aliStubUser1.loadUsers(RequestLoadUsers(Seq(sara.userId)))

    rsp.users should have size 1
    rsp.users.head should have(
      'id(sara.userId),
      'name(sara.name.get),
      'localName(sara.name.get),
      'about(None),
      'contactsRecord(Seq(ApiContactRecord().withPhoneNumber(sara.phoneNumber.get))))
  }

  ignore should "load more than one user" in {
    val users = createUsersWithPhone(5)

    val userStubUser1 = userStub.withInterceptors(clientTokenInterceptor(users.head.token))

    val rsp = userStubUser1.loadUsers(RequestLoadUsers(users.map(_.userId)))

    rsp.users should have size users.size
    rsp.users shouldBe users.map(u ⇒ ApiUser(
      id = u.userId,
      name = u.name.get,
      localName = u.name.get,
      about = None,
      contactsRecord = Seq(ApiContactRecord().withPhoneNumber(u.phoneNumber.get))))

  }

  it should "load user with local name" in {
    val users = createUsersWithPhone(5)

    val contactStubUser1 = contactStub.withInterceptors(clientTokenInterceptor(users.head.token))
    val userStubUser1 = userStub.withInterceptors(clientTokenInterceptor(users.head.token))

    //Adds 3 user to contact of user0
    val requests = users.slice(1, 4).zipWithIndex.map {
      case (user, index) ⇒
        RequestAddContact(
          localName = s"user-0$index",
          contactRecord = Some(ApiContactRecord().withPhoneNumber(user.phoneNumber.get)))
    }

    requests foreach contactStubUser1.addContact

    val rsp = userStubUser1.loadUsers(RequestLoadUsers(users.slice(1, 4).map(_.userId)))

    rsp shouldBe 3

  }

}
