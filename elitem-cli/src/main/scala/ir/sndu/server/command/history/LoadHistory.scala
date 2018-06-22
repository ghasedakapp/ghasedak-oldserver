package ir.sndu.server.command.history

import ir.sndu.server.PeerHelper
import ir.sndu.server.command.CommandBase
import picocli.CommandLine
import ir.sndu.server.GrpcStubs._
import ir.sndu.server.messaging.RequestLoadHistory
import ir.sndu.server.ApiConversions._
import ir.sndu.server.command.AuthHelper.authenticate
@CommandLine.Command(
  name = "history",
  description = Array("Load History"))
class LoadHistory extends CommandBase {

  @CommandLine.Option(
    names = Array("-i", "--id"),
    description = Array("Unique Id"))
  private var uniqueID: Long = _

  @CommandLine.Option(
    names = Array("-l", "--limit"),
    description = Array("Number of dialogs"))
  private var limit: Int = 10

  override def run(): Unit =
    authenticate { implicit client =>
      val peer = PeerHelper.fromUniqueId(uniqueID)
      val rsp = messagingStub.loadHistory(RequestLoadHistory(
        Some(peer.toOutPeer(client.userId)),
        System.currentTimeMillis(),
        limit, client.token))

      println(rsp)
    }
}
