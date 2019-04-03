package im.ghasedak.server.chat

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor._
import im.ghasedak.api.chat.{ Chat, ChatType, Member }
import im.ghasedak.rpc.chat.ResponseCreateChat
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.repo.chat.{ ChatRepo, ChatUserRepo }
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random
import im.ghasedak.server.model.TimeConversions._
import im.ghasedak.server.utils.concurrent.DBIOResult
final class ChatExtensionImpl(system: ExtendedActorSystem) extends Extension {

  private implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for user extension jobs and users actor
  private implicit val ec: ExecutionContext = system.dispatcher

  private implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

  def createChat(clientUserId: Int, randomId: Long, chatType: Int, title: String, users: Seq[Int], isSame: Boolean = true): Future[ResponseCreateChat] = {
    val members = users.distinct
    val chatId = Random.nextLong()
    val date = Instant.now()
    val chat = Chat(
      id = chatId,
      chatType = chatType,
      title = title,
      creatorUserId = clientUserId,
      members =
        members.map(Member(_, clientUserId, Some(date), false)) ++
          Seq(Member(clientUserId, clientUserId, Some(date), true)),
      membersAmount = Some(members.length),
      createdAt = Some(date))
    val action = for {
      _ ← ChatRepo.create(chat, randomId)
      _ ← ChatUserRepo.create(chatId, clientUserId, clientUserId, LocalDateTime.ofInstant(date, ZoneOffset.UTC), None, true)
      _ ← ChatUserRepo.create(chatId, users.toSet, clientUserId, LocalDateTime.ofInstant(date, ZoneOffset.UTC), None)
    } yield ResponseCreateChat(Some(chat), Some(date))

    db.run(action)
  }

  def inviteUser(clientUserId: Int, randomId: Long, chatId: Long, userId: Int): Future[ResponseVoid] = {
    val date = Instant.now()
    val action = for {
      _ ← ChatUserRepo.create(chatId, clientUserId, clientUserId, LocalDateTime.ofInstant(date, ZoneOffset.UTC), None, true)
      _ ← ChatUserRepo.create(chatId, userId, clientUserId, LocalDateTime.ofInstant(date, ZoneOffset.UTC), None, false)
    } yield ResponseVoid()

    db.run(action)
  }

}

object ChatExtension extends ExtensionId[ChatExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ChatExtensionImpl = new ChatExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = ChatExtension
}
