package ir.sndu.server.contacts

import ir.sndu.api.peer._
import ir.sndu.server.auth.AuthHelper
import ir.sndu.rpc.contact.RequestSearchContacts
import ir.sndu.server.{ ClientData, GrpcBaseSuit }

class ContactSpec extends GrpcBaseSuit
  with AuthHelper {

  "search contact" should "return correct out peer" in searchContact

  def searchContact(): Unit = {
    val ClientData(user1, token1, number) = createUser()

    contactStub.searchContacts(RequestSearchContacts(number.toString))
      .peers shouldBe Seq(ApiOutPeer(ApiPeerType.Private, user1.id))

  }

}
