package ir.sndu.server.auth

import ir.sndu.persist.db.PostgresDb
import org.scalatest._

class AuthSpec extends FlatSpec with Matchers {

  "auth" should "pass" in login

  def login: Unit = {
    PostgresDb.db
  }

}
