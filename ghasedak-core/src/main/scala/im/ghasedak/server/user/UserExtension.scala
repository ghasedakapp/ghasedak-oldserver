package im.ghasedak.server.user

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor._
import cats.syntax.flatMap
import im.ghasedak.api.contact.ContactRecord
import im.ghasedak.api.messaging.MessageContent
import im.ghasedak.api.user.User
import im.ghasedak.rpc.messaging.ResponseSendMessage
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.repo.contact.{ UserContactRepo, UserPhoneContactRepo }
import im.ghasedak.server.repo.dialog.DialogRepo
import im.ghasedak.server.repo.user.UserRepo
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {

  private implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for user extension jobs and users actor
  private implicit val ec: ExecutionContext = system.dispatcher

  private implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

  import im.ghasedak.server.messaging.HistoryUtils._

  private def calculateDate: Instant = {
    // todo: avoids duplicate date
    Instant.now()
  }

  def sendMessage(userId: Int, chatId: Long, randomId: Long, message: MessageContent): Future[ResponseSendMessage] = {
    val msgDate = calculateDate
    val msgLocalDate = LocalDateTime.ofInstant(msgDate, ZoneOffset.UTC)
    val action = for {
      seq ← writeHistoryMessage(
        chatId,
        userId,
        randomId,
        msgLocalDate,
        message)
      _ ← DialogRepo.updateLastMessageSeqDate(chatId, seq, msgLocalDate)
      _ ← DialogRepo.updateOwnerLastReadSeq(chatId, seq)
    } yield ResponseSendMessage(seq, msgDate.toEpochMilli)
    db.run(action)
  }

  def messageReceived(userId: Int, chatId: Long, seq: Int): Future[ResponseVoid] = {
    val action = for {
      _ ← DialogRepo.updateOwnerLastReceivedSeq(chatId, seq)
      _ ← DialogRepo.updateLastReceivedSeq(chatId, seq)
    } yield ResponseVoid()
    db.run(action)
  }

  def messageRead(userId: Int, chatId: Long, seq: Int): Future[ResponseVoid] = {
    val action = for {
      _ ← DialogRepo.updateOwnerLastReadSeq(chatId, seq)
      _ ← DialogRepo.updateLastReadSeq(chatId, seq)
    } yield ResponseVoid()
    db.run(action)
  }

}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserExtensionImpl = new UserExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserExtension
}
