package im.ghasedak.server.repo.chat

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.chat.ChatUserModel
import im.ghasedak.server.repo.TypeMapper._
import slick.dbio.Effect.Write
import slick.lifted.Tag
import slick.sql.FixedSqlAction

final class ChatUsersTable(tag: Tag) extends Table[ChatUserModel](tag, "chat_users") {
  def chatId = column[Long]("chat_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def inviterUserId = column[Int]("inviter_user_id")

  def invitedAt = column[LocalDateTime]("invited_at")

  def joinedAt = column[Option[LocalDateTime]]("joined_at")

  def isAdmin = column[Boolean]("is_admin")

  def * = (chatId, userId, inviterUserId, invitedAt, joinedAt, isAdmin) <> (ChatUserModel.tupled, ChatUserModel.unapply)
}

object ChatUserRepo {

  val chatUsers = TableQuery[ChatUsersTable]
  val chatUsersC = Compiled(chatUsers)

  def byPK(chatId: Rep[Long], userId: Rep[Int]) = chatUsers filter (g ⇒ g.chatId === chatId && g.userId === userId)
  def byGroupId(chatId: Rep[Long]) = chatUsers filter (_.chatId === chatId)
  def byUserId(userId: Rep[Int]) = chatUsers filter (_.userId === userId)

  def joinedAtByPK(chatId: Rep[Long], userId: Rep[Int]) = byPK(chatId, userId) map (_.joinedAt)
  def userIdByGroupId(chatId: Rep[Long]) = byGroupId(chatId) map (_.userId)

  val byPKC = Compiled(byPK _)
  val byGroupIdC = Compiled(byGroupId _)
  val byUserIdC = Compiled(byUserId _)

  val userIdByGroupIdC = Compiled(userIdByGroupId _)
  val joinedAtByPKC = Compiled(joinedAtByPK _)

  def create(chatId: Long, userId: Int, inviterUserId: Int, invitedAt: LocalDateTime, joinedAt: Option[LocalDateTime], isAdmin: Boolean) =
    chatUsersC += ChatUserModel(chatId, userId, inviterUserId, invitedAt, joinedAt, isAdmin)

  def create(chatId: Long, userIds: Set[Int], inviterUserId: Int, invitedAt: LocalDateTime, joinedAt: Option[LocalDateTime]) =
    chatUsersC ++= userIds.map(ChatUserModel(chatId, _, inviterUserId, invitedAt, joinedAt, isAdmin = false))

  def find(chatId: Int) =
    byGroupIdC(chatId).result

  def find(chatId: Int, userId: Int) =
    byPKC((chatId, userId)).result.headOption

  def exists(chatId: Int, userId: Int) =
    byPKC.applied((chatId, userId)).exists.result

  def isJoined(chatId: Int, userId: Int) =
    byPKC.applied((chatId, userId)).map(_.joinedAt.isDefined).result.headOption

  def findByUserId(userId: Int) =
    byUserIdC(userId).result

  def findUserIds(chatId: Int) =
    userIdByGroupIdC(chatId).result

  def findUserIds(chatIds: Set[Long]) =
    chatUsers.filter(_.chatId inSetBind chatIds).map(_.userId).result

  def setJoined(chatId: Int, userId: Int, date: LocalDateTime) =
    joinedAtByPKC((chatId, userId)).update(Some(date))

  def delete(chatId: Int, userId: Int): FixedSqlAction[Int, NoStream, Write] =
    byPKC.applied((chatId, userId)).delete

  def makeAdmin(chatId: Int, userId: Int) =
    byPKC.applied((chatId, userId)).map(_.isAdmin).update(true)

}
