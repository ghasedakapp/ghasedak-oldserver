package im.ghasedak.server.user

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor._
import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.api.messaging.ApiMessage
import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import im.ghasedak.api.user.ApiUser
import im.ghasedak.rpc.messaging.ResponseSendMessage
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.repo.dialog.DialogRepo
import im.ghasedak.server.repo.user.UserRepo
import im.ghasedak.server.model.contact.UserContact
import im.ghasedak.server.model.user.UserAuth
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {

  private implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for user extension jobs and users actor
  private implicit val ec: ExecutionContext = system.dispatcher

  private implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

  import im.ghasedak.server.dialog.DialogUtils._
  import im.ghasedak.server.messaging.HistoryUtils._

  private def calculateDate: Instant = {
    // todo: avoids duplicate date
    Instant.now()
  }

  def sendMessage(userId: Int, peer: ApiPeer, randomId: Long, message: ApiMessage): Future[ResponseSendMessage] = {
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
      _ ← DialogRepo.updateOwnerLastReadSeq(userId, peer, seq)
      _ ← createOrUpdateDialog(peer.id, selfPeer, seq, msgLocalDate)
    } yield ResponseSendMessage(seq, msgDate.toEpochMilli)
    db.run(action)
  }

  def messageReceived(userId: Int, peer: ApiPeer, seq: Int): Future[ResponseVoid] = {
    val action = for {
      _ ← DialogRepo.updateOwnerLastReceivedSeq(userId, peer, seq)
      _ ← DialogRepo.updateLastReceivedSeq(userId, peer, seq)
    } yield ResponseVoid()
    db.run(action)
  }

  def messageRead(userId: Int, peer: ApiPeer, seq: Int): Future[ResponseVoid] = {
    val action = for {
      _ ← DialogRepo.updateOwnerLastReadSeq(userId, peer, seq)
      _ ← DialogRepo.updateLastReadSeq(userId, peer, seq)
    } yield ResponseVoid()
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
