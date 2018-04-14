package ir.sndu.server.auth

import ir.sndu.server.users.ApiSex
import ir.sndu.server.{ GrpcBaseSuit, UserInfo }

import scala.util.Random

trait AuthHelper {
  self: GrpcBaseSuit =>

  def createUser(): UserInfo = {
    val number = Random.nextLong()
    val rsp = authStub.signUp(RequestSignUp(
      name = "Amir",
      sex = ApiSex.Male,
      number = number))
    UserInfo(rsp.user.get, rsp.token, number)
  }
}
