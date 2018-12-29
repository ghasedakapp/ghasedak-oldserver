package ir.sndu.server.user

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.util.Timeout
import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.api.messaging.ApiMessage
import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import im.ghasedak.api.user.ApiUser
import ir.sndu.persist.db.DbExtension
import ir.sndu.persist.repo.user.UserRepo
import ir.sndu.server.model.user.UserAuth
import slick.jdbc.PostgresProfile

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {

  implicit private val _system: ActorSystem = system
  implicit private val timeout: Timeout = Timeout(30.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

  import DialogUtils._
  import HistoryUtils._

  private def calculateDate: Instant = {
    //TODO Avoids duplicate date
    Instant.now()
  }

  def sendMessage(userId: Int, peer: ApiPeer, randomId: Long, message: ApiMessage): Future[Unit] = {
    val msgDate = calculateDate
    val msgLocalDate = LocalDateTime.ofInstant(msgDate, ZoneOffset.UTC)
    val selfPeer = ApiPeer(ApiPeerType.PRIVATE, userId)
    val action = for {
      seq ← writeHistoryMessage(
        selfPeer,
        peer,
        randomId,
        msgLocalDate,
        message)
      _ ← createOrUpdateDialog(userId, peer, seq, msgLocalDate)
      _ ← createOrUpdateDialog(peer.id, selfPeer, seq, msgLocalDate)
    } yield ()
    db.run(action)
  }

  def getUsers(clientOrgId: Int, clientUserId: Int, userIds: Seq[Int]): Future[Seq[ApiUser]] = {
    val action =
      UserRepo.findUserContact(clientOrgId, clientUserId, userIds) map (_.map {
        case ((user, userAuth), contact) ⇒
          ApiUser(
            id = user.id,
            name = user.name,
            localName = contact.map(_.localName).getOrElse(user.name),
            about = user.about,
            contactsRecord = userAuth.map(_.toApiContact).getOrElse(Seq.empty))
      })
    db.run(action)
  }

}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserExtensionImpl = new UserExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserExtension
}
