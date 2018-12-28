package ir.sndu.server.user

import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.rpc.contact.RequestAddContact
import im.ghasedak.rpc.user.RequestLoadUsers
import ir.sndu.server.GrpcBaseSuit

class UserServiceSpec extends GrpcBaseSuit {

  behavior of "UserServiceImpl"

  it should "get user info" in {
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
