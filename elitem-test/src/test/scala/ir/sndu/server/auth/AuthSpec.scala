package ir.sndu.server.auth

import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.user._
import ir.sndu.server.GrpcBaseSuit
import ir.sndu.api.user.ApiSex
import ir.sndu.server.model.user._
import ir.sndu.rpc.auth.RequestSignUp

import scala.util.Random

class AuthSpec extends GrpcBaseSuit {

  "auth" should "pass" in login
  "multiple signup" should "get same userId" in signup

  def login(): Unit = {
    val id = ThreadLocalRandom.current().nextInt()
    PostgresDb.db.run(UserRepo.create(User(id, "", "", "IR", Sex.Female, UserState.Registered, LocalDateTime.now()))).futureValue
    PostgresDb.db.run(UserPhoneRepo.create(ThreadLocalRandom.current().nextInt(), id, "", 98935, "")).futureValue
  }

  def signup(): Unit = {
    val number = Random.nextLong()
    val req = RequestSignUp(
      name = "Amir",
      sex = ApiSex.Male,
      number = number)
    val response1 = authStub.signUp(req)
    val response2 = authStub.signUp(req)

    response1.user.get.name shouldBe "Amir"

    response1.user.get.id shouldBe response2.user.get.id
  }

}
