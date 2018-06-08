package ir.sndu.server.command.messaging

import ir.sndu.server.ElitemConsole.withError
import ir.sndu.server.GrpcStubs._
import ir.sndu.server.command.AuthHelper._
import ir.sndu.server.command.{ ClientData, CommandBase }
import ir.sndu.server.contacts.RequestSearchContacts
import ir.sndu.server.message.{ ApiMessage, ApiTextMessage }
import ir.sndu.server.messaging.RequestSendMessage
import picocli.CommandLine

import scala.util.Random
@CommandLine.Command(
  name = "sendmessage",
  description = Array("Send Message to Specific user by phone number"))
class SendMesage extends CommandBase {

  private def send(number: Long, msg: String)(implicit client: ClientData): Unit = {

    val peers = contactsStub.searchContacts(RequestSearchContacts(number.toString, client.token)).peers

    if (peers.isEmpty)
      withError(System.err.println("Phone number does not exists"))
    else
      messagingStub.sendMessage(RequestSendMessage(
        outPeer = Some(peers.head),
        randomId = Random.nextLong(),
        message = Some(ApiMessage().withTextMessage(ApiTextMessage(msg))),
        client.token))

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
    authenticate { implicit client =>
      send(peer, message)
    }
}
