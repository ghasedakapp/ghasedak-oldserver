package ir.sndu.server.auth

import ir.sndu.server.GrpcBaseSuit
import ir.sndu.server.users.{ ApiSex, ApiUser }

import scala.util.Random

trait AuthHelper {
  self: GrpcBaseSuit =>

  case class UserInfo(user: ApiUser, token: String, number: Long)
  def createUser(): UserInfo = {
    val number = Random.nextLong()
    val rsp = authStub.signUp(RequestSignUp(
      name = "Amir",
      sex = ApiSex.Male,
      number = number))
    UserInfo(rsp.user.get, rsp.token, number)
  }
}
