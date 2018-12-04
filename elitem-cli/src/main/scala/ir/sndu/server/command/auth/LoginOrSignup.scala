package ir.sndu.server.command.auth

import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.GrpcStubs._
import ir.sndu.api.user.ApiSex
import ir.sndu.server.command.CommandBase
import ir.sndu.server.db.DbHelper._
import ir.sndu.rpc.auth.RequestSignUp
import org.iq80.leveldb.DB
import picocli.CommandLine

@CommandLine.Command(
  name = "login",
  description = Array("@|bold Login|@ or @|underline Signup|@"))
class LoginOrSignup extends CommandBase {

  private def signup()(implicit db: DB): Unit = {
    val req = RequestSignUp(
      name = Option(name).getOrElse(""),
      sex = ApiSex.Male,
      number = phone)

    val rsp = authStub.signUp(req)
    db.put("token", rsp.token)
    db.putBytes("user", rsp.user.get.toByteArray)
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
    leveldb { implicit db ⇒
      db.get("token") match {
        case Some(token) ⇒ withError {
          System.err.println("You are logged in")
        }
        case None ⇒ signup()
      }

    }
}
