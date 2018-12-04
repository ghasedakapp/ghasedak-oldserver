package ir.sndu.server.command

import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.db.DbHelper._
import ir.sndu.api.user.ApiUser

case class ClientData(token: String, userId: Int)
object AuthHelper {
  def authenticate(f: ClientData ⇒ Unit)(
    implicit
    log:    org.slf4j.Logger,
    parent: ElitemCmd): Unit = {
    leveldb { implicit db ⇒
      db.get("token").flatMap(token ⇒ db.getBytes("user").map(u ⇒ ClientData(token, ApiUser.parseFrom(u).id)))
    }.get match {
      case Some(client) ⇒ f(client)
      case None         ⇒ withError(System.err.println("Please login at first"))
    }
  }
}
