package ir.sndu.server.command.dialog

import ir.sndu.server.GrpcStubs._
import ir.sndu.server.command.AuthHelper._
import ir.sndu.server.command.{ ClientData, CommandBase }
import ir.sndu.server.messaging.RequestLoadDialogs
import ir.sndu.server.rpc.users.RequestLoadFullUsers
import picocli.CommandLine

@CommandLine.Command(
  name = "dialogs",
  description = Array("Load Dialogs"))
class LoadDialog extends CommandBase {

  import ir.sndu.server.ApiConversions._
  private def load(limit: Int)(implicit client: ClientData): Unit = {
    val rsp = messagingStub.loadDialogs(RequestLoadDialogs(10, client.token))
    val userPeers = rsp.dialogs.flatMap(_.peer.flatMap(p => p.toUserOutPeer))

    userStub.loadFullUsers(RequestLoadFullUsers(userPeers, client.token))
    rsp.dialogs.foreach(println)
  }

  @CommandLine.Option(
    names = Array("-l", "--limit"),
    description = Array("Number of dialogs"))
  private var limit: Int = _

  override def run(): Unit =
    authenticate { implicit client =>
      load(limit)
    }
}
