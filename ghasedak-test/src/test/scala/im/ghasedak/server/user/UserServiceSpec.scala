package im.ghasedak.server.user

import im.ghasedak.api.user.{ ContactRecord, User, UserData, UserProfile }
import im.ghasedak.api.user.ContactType.CONTACTTYPE_PHONE
import im.ghasedak.rpc.contact.RequestAddContact
import im.ghasedak.rpc.user.RequestLoadUsers
import im.ghasedak.server.GrpcBaseSuit

class UserServiceSpec extends GrpcBaseSuit {

  behavior of "UserServiceImpl"

  it should "load user" in {
    val ali = createUserWithPhone()
    val sara = createUserWithPhone()

    val aliStubUser1 = userStub.loadUsers.addHeader(tokenMetadataKey, ali.token)

    val rsp = aliStubUser1.invoke(RequestLoadUsers(Seq(sara.userId))).futureValue

    rsp.profiles should have size 1

    val userProfile = rsp.profiles.head
    val userInfo = userProfile.user.get
    val userData = userInfo.data.get

    userInfo.id shouldBe sara.userId

    userData.name shouldBe sara.name.get

    val contactInfo = Seq(ContactRecord(
      CONTACTTYPE_PHONE,
      longValue = Some(sara.phoneNumber.get),
      title = Some("Mobile Phone")))

    userProfile should have(
      'about(None),
      'contactInfo(contactInfo))
  }

  it should "load more than one user" in {
    val users = createUsersWithPhone(5)

    val userStubUser1 = userStub.loadUsers.addHeader(tokenMetadataKey, users.head.token)

    val rsp = userStubUser1.invoke(RequestLoadUsers(users.map(_.userId))).futureValue

    rsp.profiles should have size users.size

    val userProfiles = rsp.profiles
    val userInfos = userProfiles.map(_.user.get)
    val userDatas = userInfos.map(_.data.get)

    val contactInfos = users.map { u ⇒
      Seq(ContactRecord(
        CONTACTTYPE_PHONE,
        longValue = Some(u.phoneNumber.get),
        title = Some("Mobile Phone")))
    }

    userInfos.map(_.id) shouldBe users.map(_.userId)

    userDatas.map(_.name) shouldBe users.map(_.name.get)

    userProfiles.map(_.contactInfo) shouldBe contactInfos

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
          localName = Some(s"user-${user.userId}-$index"),
          contactRecord = Some(ContactRecord().withType(CONTACTTYPE_PHONE).withLongValue(user.phoneNumber.get)))
    }

    requests foreach (contactStubUser1.invoke(_).futureValue)
    requests foreach (contactStubUser2.invoke(_).futureValue)

    val rsp = userStubUser1.invoke(RequestLoadUsers(userForContact.map(_.userId))).futureValue

    val usersLocalname = userForContact.zipWithIndex.map {
      case (user, index) ⇒ s"user-${user.userId}-$index"
    }

    rsp.profiles should have size userForContact.size

    rsp.profiles.map(_.user.get.data.get.name) shouldBe usersLocalname

  }

}
