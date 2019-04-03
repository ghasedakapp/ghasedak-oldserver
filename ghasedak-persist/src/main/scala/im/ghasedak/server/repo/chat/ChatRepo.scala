package im.ghasedak.server.repo.chat

import java.time.Instant

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.api.chat.Chat
import im.ghasedak.server.model.TimeConversions._
import im.ghasedak.server.model.chat.ChatModel
import im.ghasedak.server.repo.TypeMapper._
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

final class ChatTable(tag: Tag) extends Table[ChatModel](tag, "chats") {
  def id = column[Long]("id", O.PrimaryKey)

  def chatType = column[Int]("type")

  def creatorUserId = column[Int]("creator_user_id")

  def title = column[String]("title")

  def createdAt = column[Instant]("created_at")

  def titleChangerUserId = column[Int]("title_changer_user_id")

  def titleChangedAt = column[Instant]("title_changed_at")

  def titleChangeRandomId = column[Long]("title_change_random_id")

  def avatarChangerUserId = column[Int]("avatar_changer_user_id")

  def avatarChangedAt = column[Instant]("avatar_changed_at")

  def avatarChangeRandomId = column[Long]("avatar_change_random_id")

  def about = column[Option[String]]("about")

  def nick = column[Option[String]]("nick")

  def * =
    (
      id,
      chatType,
      creatorUserId,
      title,
      createdAt,
      titleChangerUserId,
      titleChangedAt,
      titleChangeRandomId,
      avatarChangerUserId,
      avatarChangedAt,
      avatarChangeRandomId,
      about,
      nick) <> (ChatModel.tupled, ChatModel.unapply)

}

object ChatRepo {
  val groups = TableQuery[ChatTable]
  val groupsC = Compiled(groups)

  def byId(id: Rep[Long]) = groups filter (_.id === id)
  def titleById(id: Rep[Long]) = byId(id) map (_.title)

  val byIdC = Compiled(byId _)
  val titleByIdC = Compiled(titleById _)

  val allIds = groups.map(_.id)

  def create(chat: Chat, randomId: Long) =
    groups.insertOrUpdate(
      ChatModel(
        id = chat.id,
        chatType = chat.chatType,
        creatorUserId = chat.creatorUserId,
        title = chat.title,
        createdAt = chat.createdAt.get,
        titleChangerUserId = chat.creatorUserId,
        titleChangedAt = chat.createdAt.get,
        titleChangeRandomId = randomId,
        avatarChangerUserId = chat.creatorUserId,
        avatarChangedAt = chat.createdAt.get,
        avatarChangeRandomId = randomId))

  def create(clientUserId: Int, id: Long, chatType: Int, title: String, createdAt: Instant, randomId: Long) =
    groups.insertOrUpdate(
      ChatModel(
        id = id,
        chatType = chatType,
        creatorUserId = clientUserId,
        title = title,
        createdAt = createdAt,
        titleChangerUserId = clientUserId,
        titleChangedAt = createdAt,
        titleChangeRandomId = randomId,
        avatarChangerUserId = clientUserId,
        avatarChangedAt = createdAt,
        avatarChangeRandomId = randomId))

  def findAllIds = allIds.result

  def find(id: Long): SqlAction[Option[ChatModel], NoStream, Effect.Read] =
    byIdC(id).result.headOption

  def findTitle(id: Long): SqlAction[Option[String], NoStream, Effect.Read] =
    titleByIdC(id).result.headOption

  def updateTitle(id: Long, title: String, changerUserId: Int, randomId: Long, date: Instant): FixedSqlAction[Int, NoStream, Effect.Write] =
    byIdC.applied(id)
      .map(g ⇒ (g.title, g.titleChangerUserId, g.titleChangedAt, g.titleChangeRandomId))
      .update((title, changerUserId, date, randomId))

  def updateNick(id: Long, nick: Option[String]): FixedSqlAction[Int, NoStream, Effect.Write] =
    byIdC.applied(id).map(_.nick).update(nick)

  def updateAbout(id: Long, about: Option[String]): FixedSqlAction[Int, NoStream, Effect.Write] =
    byIdC.applied(id).map(_.about).update(about)

}
