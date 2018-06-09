package ir.sndu.server.command

import ir.sndu.server.CliConfigs
import ir.sndu.server.ElitemConsole.{ withError, withWarning }
import ir.sndu.server.db.DbHelper._

case class ClientData(token: String)
object AuthHelper {
  def authenticate(f: ClientData => Unit)(
    implicit
    log: org.slf4j.Logger,
    parent: ElitemCmd): Unit = {
    leveldb { implicit db =>
      db.get("token")
    }.get match {
      case Some(token) => f(ClientData(token))
      case None => withError(System.err.println("Please login at first"))
    }
  }
}
