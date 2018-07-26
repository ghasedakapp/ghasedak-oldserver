package ir.sndu.server.command.history

import java.time.{ Instant, LocalDateTime, ZoneId, ZoneOffset }

import ir.sndu.server.PeerHelper
import ir.sndu.server.command.{ ClientData, CommandBase }
import picocli.CommandLine
import ir.sndu.server.GrpcStubs._
import ir.sndu.server.messaging.RequestLoadHistory
import ir.sndu.server.ApiConversions._
import ir.sndu.server.command.AuthHelper.authenticate
import ir.sndu.server.message.{ ApiMessage, ApiMessageContainer }
import ir.sndu.server.ElitemConsole._

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

  private def loadBefore()(implicit client: ClientData): Seq[ApiMessageContainer] = {
    val peer = PeerHelper.fromUniqueId(uniqueID)
    messagingStub.loadHistory(RequestLoadHistory(
      Some(peer.toOutPeer(client.userId)),
      System.currentTimeMillis(),
      limit, client.token)).history
  }

  private def formatMessages(messages: Seq[ApiMessageContainer]): Seq[String] = {
    messages.flatMap(m ⇒ m.message.map(_.getTextMessage.text +
      "  |  " +
      LocalDateTime.ofInstant(Instant.ofEpochMilli(m.date), ZoneOffset.ofHoursMinutes(4, 30))))

  }

  override def run(): Unit =
    authenticate { implicit client ⇒
      withOutput(
        formatMessages(loadBefore()).foldLeft("")(_ + _ + "\n"))
    }
}
