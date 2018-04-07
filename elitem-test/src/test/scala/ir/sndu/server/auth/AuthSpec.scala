package ir.sndu.server.auth

import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.{ UserPhoneRepo, UserRepo }
import ir.sndu.server.model.user.{ Sex, User, UserPhone, UserState }
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class AuthSpec extends FlatSpec with Matchers with ScalaFutures {

  "auth" should "pass" in login

  def login: Unit = {
    val id = ThreadLocalRandom.current().nextInt()
    PostgresDb.db.run(UserRepo.create(User(id, "", "", "IR", Sex.Female, UserState.Registered, LocalDateTime.now()))).futureValue
    PostgresDb.db.run(UserPhoneRepo.create(ThreadLocalRandom.current().nextInt(), id, "", 98935, ""))
  }

}
