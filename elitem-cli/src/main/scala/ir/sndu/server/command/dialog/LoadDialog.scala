package ir.sndu.server.command.dialog

import java.time.{ Instant, ZoneOffset }

import ir.sndu.server.GrpcStubs._
import ir.sndu.server.PeerHelper
import ir.sndu.server.command.AuthHelper._
import ir.sndu.server.command.{ ClientData, CommandBase }
import ir.sndu.server.message.ApiDialog
import ir.sndu.server.messaging.RequestLoadDialogs
import ir.sndu.server.peer.{ ApiPeer, ApiPeerType, ApiUserOutPeer }
import ir.sndu.server.rpc.users.RequestLoadFullUsers
import ir.sndu.server.users.ApiUser
import org.iq80.leveldb.DB
import picocli.CommandLine

@CommandLine.Command(
  name = "dialogs",
  description = Array("Load Dialogs"))
class LoadDialog extends CommandBase {

  case class LocalDialog(
    uniqueId: String = "",
    name:     String = "",
    peerType: String = "",
    msg:      String = "",
    date:     String = "",
    counter:  String = "0")

  import ir.sndu.server.ApiConversions._
  import ir.sndu.server.db.DbHelper._
  private def getUser(userPeer: ApiUserOutPeer)(implicit db: DB): Option[ApiUser] =
    db.getBytes(userPeer.userId.toString).map(ApiUser.parseFrom)

  private def fillMissing(dialogs: Seq[ApiDialog])(implicit client: ClientData, db: DB) {
    val userPeers = dialogs.flatMap(_.peer.flatMap(p ⇒ p.toUserOutPeer))
    userStub.loadFullUsers(RequestLoadFullUsers(
      userPeers.filter(getUser(_).isEmpty))).fullUsers.foreach { u ⇒
      db.putBytes(u.id.toString, u.toByteArray)
      //TODO load full groups

    }
  }

  private def getDialog(user: ApiUser, dialog: ApiDialog): LocalDialog = {
    val message = dialog.message.get.getTextMessage.text
    LocalDialog(
      PeerHelper.toUniqueId(ApiPeer(ApiPeerType.Private, user.id)).toString,
      user.name,
      "Private",
      if (message.size > 20) message.substring(0, 15) + "..." else message,
      Instant.ofEpochMilli(dialog.date).atZone(ZoneOffset.ofHoursMinutes(4, 30)).toString)
  }
  private def load(limit: Int)(implicit client: ClientData): Unit =
    leveldb { implicit db ⇒
      val rsp = messagingStub.loadDialogs(RequestLoadDialogs(limit))
      fillMissing(rsp.dialogs)

      val localDialogs = rsp.dialogs.map { dialog ⇒
        val peer = dialog.peer.get
        peer.`type` match {
          case ApiPeerType.Private ⇒
            val user = getUser(ApiUserOutPeer(peer.id)).getOrElse(ApiUser())
            getDialog(user, dialog)
          case ApiPeerType.Group ⇒ LocalDialog()
        }
      }
      val dialogs = localDialogs.zipWithIndex.map(record ⇒ (record._2 + 1, record._1)).toMap
      printDialogs(dialogs)
    }

  private def formatDialog(dialog: LocalDialog): String =
    s"[uid:${dialog.uniqueId}, name:${dialog.name}, type:${dialog.peerType}, msg:${dialog.msg}, date:${dialog.date}]"

  private def printDialogs(dialogs: Map[Int, LocalDialog]): Unit = {
    dialogs.toSeq.foreach {
      case (index, dialog) ⇒ println(s"$index-> ${formatDialog(dialog)}")
    }
  }

  @CommandLine.Option(
    names = Array("-l", "--limit"),
    description = Array("Number of dialogs"))
  private var limit: Int = 10

  override def run(): Unit =
    authenticate { implicit client ⇒
      load(limit)
    }
}
