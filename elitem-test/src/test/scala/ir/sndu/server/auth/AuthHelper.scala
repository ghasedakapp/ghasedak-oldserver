package ir.sndu.server.auth

import ir.sndu.api.user.ApiSex
import ir.sndu.rpc.auth.RequestSignUp
import ir.sndu.server._

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
