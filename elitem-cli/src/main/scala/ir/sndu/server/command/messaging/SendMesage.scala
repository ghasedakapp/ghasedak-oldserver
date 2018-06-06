package ir.sndu.server.command.messaging

import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.GrpcStubs._
import ir.sndu.server.command.{ ClientData, CommandBase }
import ir.sndu.server.db.DbHelper._
import ir.sndu.server.messaging.{ RequestLoadDialogs, RequestSendMessage }
import picocli.CommandLine
import ir.sndu.server.command.AuthHelper._
import ir.sndu.server.message.{ ApiMessage, ApiTextMessage }
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }
@CommandLine.Command(
  name = "sendmessage",
  description = Array("Send MEssage to Specific user by phone number"))
class SendMesage extends CommandBase {

  private def send(peer: Long, msg: String)(implicit client: ClientData): Unit = {

    //    val outPeer2 = ApiOutPeer(ApiPeerType.Private, user2.id)

    val msg1 = ApiMessage().withTextMessage(ApiTextMessage(msg))
    RequestSendMessage()
  }

  @CommandLine.Option(
    names = Array("-l", "--limit"),
    description = Array("Number of dialogs"))
  private var limit: Int = _

  override def run(): Unit =
    authenticate { implicit client =>
      send(1L, "")
    }
}
