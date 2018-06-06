package ir.sndu.server.contacts

import ir.sndu.server.auth.AuthHelper
import ir.sndu.server.{ GrpcBaseSuit, UserInfo }

class ContactSpec extends GrpcBaseSuit
  with AuthHelper {

  "search contact" should "" in searchContact

  def searchContact(): Unit = {
    val UserInfo(user1, token1, _) = createUser()
    val UserInfo(user2, token2, _) = createUser()
  }

}
