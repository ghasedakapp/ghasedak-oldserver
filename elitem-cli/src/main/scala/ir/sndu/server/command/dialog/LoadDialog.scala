package ir.sndu.server.command.dialog

import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.GrpcStubs._
import ir.sndu.server.command.CommandBase
import ir.sndu.server.db.DbHelper._
import ir.sndu.server.messaging.RequestLoadDialogs
import ir.sndu.server.users.RequestLoadFullUsers
import picocli.CommandLine

@CommandLine.Command(
  name = "dialogs",
  description = Array("Load Dialogs"))
class LoadDialog extends CommandBase {

  import ir.sndu.server.ApiConversions._
  private def load(limit: Int, token: String): Unit = {
    val rsp = messagingStub.loadDialogs(RequestLoadDialogs(10, token))
    val userPeers = rsp.dialogs.flatMap(_.peer.flatMap(p => p.toUserOutPeer))

    userStub.loadFullUsers(RequestLoadFullUsers(userPeers))
    rsp.dialogs.foreach(println)
  }

  @CommandLine.Option(
    names = Array("-l", "--limit"),
    description = Array("Number of dialogs"))
  private var limit: Int = _

  override def run(): Unit =
    leveldb { implicit db =>
      db.get("token") match {
        case Some(token) => load(Option(limit).getOrElse(10), token)
        case None => withError {
          System.err.println("Please login at first")
        }
      }

    }
}
