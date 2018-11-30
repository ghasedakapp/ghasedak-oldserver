package ir.sndu.server.auth

import ir.sndu.server.apiuser.ApiSex
import ir.sndu.server.rpcauth.RequestSignUp
import ir.sndu.server.{ ClientData, GrpcBaseSuit }

import scala.util.Random

trait AuthHelper {
  self: GrpcBaseSuit ⇒

  def createUser(): ClientData = {
    val number = Random.nextLong()
    val rsp = authStub.signUp(RequestSignUp(
      name = "Amir",
      sex = ApiSex.Male,
      number = number))
    ClientData(rsp.user.get, rsp.token, number)
  }
}
