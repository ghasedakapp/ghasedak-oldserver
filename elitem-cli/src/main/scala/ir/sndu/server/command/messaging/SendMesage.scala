package ir.sndu.server.command.messaging

import io.grpc.StatusRuntimeException
import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.GrpcStubs._
import ir.sndu.api.message._
import ir.sndu.server.command.AuthHelper._
import ir.sndu.server.command.{ ClientData, CommandBase }
import ir.sndu.rpc.contact.RequestSearchContacts
import ir.sndu.rpc.messaging.RequestSendMessage
import picocli.CommandLine

import scala.util.Random

@CommandLine.Command(
  name = "sendmessage",
  description = Array("Send Message to Specific user by phone number"))
class SendMesage extends CommandBase {

  private def commonError = withError(System.err.println("Error in sending message"))

  private def send(number: Long, msg: String)(implicit client: ClientData): Unit = {

    val peers = contactsStub.searchContacts(RequestSearchContacts(number.toString)).peers

    if (peers.isEmpty)
      withError(System.err.println("Phone number does not exists"))
    else
      try {
        messagingStub.sendMessage(RequestSendMessage(
          outPeer = Some(peers.head),
          randomId = Random.nextLong(),
          message = Some(ApiMessage().withTextMessage(ApiTextMessage(msg)))))

      } catch {
        case e: StatusRuntimeException if e.getStatus.getDescription == "MESSAGE_TO_SELF" ⇒
          withError("You cant't message to yourself")
        case e: Throwable ⇒
          log.error(e.getMessage, e)
          commonError

      }

  }

  @CommandLine.Option(
    names = Array("-p", "--peer"),
    required = true,
    description = Array("Peer mobile number"))
  private var peer: Long = _

  @CommandLine.Option(
    names = Array("-m", "--message"),
    required = true,
    description = Array("Text message content"))
  private var message: String = _

  override def run(): Unit =
    authenticate { implicit client ⇒
      send(peer, message)
    }
}
