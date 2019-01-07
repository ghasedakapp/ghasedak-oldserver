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
import ir.sndu.server.model.contact.UserContact
import ir.sndu.server.model.user.UserAuth
import slick.jdbc.PostgresProfile

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

case class SendMessageAck(seq: Int, date: Long)

class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {

  implicit private val _system: ActorSystem = system
  implicit private val timeout: Timeout = Timeout(30.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

  import DialogUtils._
  import HistoryUtils._

  private def calculateDate: Instant = {
    // todo: avoids duplicate date
    Instant.now()
  }

  def sendMessage(userId: Int, peer: ApiPeer, randomId: Long, message: ApiMessage): Future[SendMessageAck] = {
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
    } yield SendMessageAck(seq, msgDate.toEpochMilli)
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
            contactsRecord = toApiContact(userAuth, contact))
      })
    db.run(action)
  }

  def toApiContact(userAuth: Option[UserAuth], contact: Option[UserContact]): Seq[ApiContactRecord] = {
    Seq(
      contact.filter(_.hasPhone) flatMap (_ ⇒ userAuth.flatMap(_.phoneNumber.map(ApiContactRecord().withPhoneNumber))),
      contact.filter(_.hasEmail) flatMap (_ ⇒ userAuth.flatMap(_.email.map(ApiContactRecord().withEmail))),
      userAuth.flatMap(_.nickname.map(ApiContactRecord().withNickname)))
      .flatten
  }

}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserExtensionImpl = new UserExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserExtension
}
