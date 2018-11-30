package ir.sndu.server.user

import java.time.{ Instant, LocalDateTime, ZoneId }

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.util.Timeout
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.apimessage.ApiMessage
import ir.sndu.server.apipeer._
import ir.sndu.server.UserCommands.SendMessageAck

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {
  implicit private val _system: ActorSystem = system
  implicit private val timeout: Timeout = Timeout(30.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val db = PostgresDb.db

  import DialogUtils._
  import HistoryUtils._

  private def calculateDate: Instant = {
    //TODO Avoids duplicate date
    Instant.now()
  }

  def sendMessage(userId: Int, peer: ApiPeer, randomId: Long, message: ApiMessage): Future[SendMessageAck] = {
    val msgDate = calculateDate
    val msgLocalDate = LocalDateTime.ofInstant(msgDate, ZoneId.systemDefault())
    val selfPeer = ApiPeer(ApiPeerType.Private, userId)
    val action = for {
      _ ← writeHistoryMessage(
        selfPeer,
        peer,
        randomId,
        msgLocalDate,
        message)
      _ ← createOrUpdateDialog(userId, peer, msgLocalDate)
      _ ← createOrUpdateDialog(peer.id, selfPeer, msgLocalDate)
    } yield SendMessageAck()
    db.run(action)

  }

}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserExtensionImpl = new UserExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserExtension
}
