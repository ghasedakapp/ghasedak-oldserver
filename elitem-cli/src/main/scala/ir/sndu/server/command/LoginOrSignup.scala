package ir.sndu.server.command

import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.GrpcStubs._
import ir.sndu.server.auth.RequestSignUp
import ir.sndu.server.disk.DbHelper._
import ir.sndu.server.users.ApiSex
import org.iq80.leveldb.DB
import picocli.CommandLine

@CommandLine.Command(
  name = "login",
  description = Array("@|bold Login|@ @|underline elitem|@ example"))
class LoginOrSignup extends Runnable {

  private def signup()(implicit db: DB): Unit = {
    val req = RequestSignUp(
      name = Option(name).getOrElse(""),
      sex = ApiSex.Male,
      number = phone)

    val rsp = authStub.signUp(req)
    db.put("token", rsp.token)
  }

  @CommandLine.Option(
    names = Array("-n", "--name"),
    description = Array("Name of user"))
  private var name: String = _

  @CommandLine.Option(
    names = Array("-p", "--phone"),
    required = true,
    description = Array("Phone Number"))
  private var phone: Long = _

  override def run(): Unit =
    leveldb() { implicit db =>
      db.get("token") match {
        case Some(token) => withError {
          System.err.println("You are logged in")
        }
        case None => signup()
      }

    }
}
