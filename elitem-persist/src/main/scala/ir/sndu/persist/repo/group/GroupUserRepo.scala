package ir.sndu.persist.repo.group

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.group.GroupUser
import slick.dbio.Effect.Write
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import slick.sql.FixedSqlAction

final class GroupUsersTable(tag: Tag) extends Table[GroupUser](tag, "group_users") {
  def groupId = column[Int]("group_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def inviterUserId = column[Int]("inviter_user_id")

  def invitedAt = column[LocalDateTime]("invited_at")

  def joinedAt = column[Option[LocalDateTime]]("joined_at")

  def isAdmin = column[Boolean]("is_admin")

  def * = (groupId, userId, inviterUserId, invitedAt, joinedAt, isAdmin) <> (GroupUser.tupled, GroupUser.unapply)
}

object GroupUserRepo {

  private val groupUsers = TableQuery[GroupUsersTable]
  private val groupUsersC = Compiled(groupUsers)

  private def byPK(groupId: Rep[Int], userId: Rep[Int]) = groupUsers filter (g ⇒ g.groupId === groupId && g.userId === userId)
  private def byGroupId(groupId: Rep[Int]) = groupUsers filter (_.groupId === groupId)
  private def byUserId(userId: Rep[Int]) = groupUsers filter (_.userId === userId)

  private def joinedAtByPK(groupId: Rep[Int], userId: Rep[Int]) = byPK(groupId, userId) map (_.joinedAt)
  private def userIdByGroupId(groupId: Rep[Int]) = byGroupId(groupId) map (_.userId)

  private val byPKC = Compiled(byPK _)
  private val byGroupIdC = Compiled(byGroupId _)
  private val byUserIdC = Compiled(byUserId _)

  private val userIdByGroupIdC = Compiled(userIdByGroupId _)
  private val joinedAtByPKC = Compiled(joinedAtByPK _)

  def create(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: LocalDateTime, joinedAt: Option[LocalDateTime], isAdmin: Boolean): DBIO[Int] = {
    groupUsersC += GroupUser(groupId, userId, inviterUserId, invitedAt, joinedAt, isAdmin)
  }

  def find(groupId: Int) =
    byGroupIdC(groupId).result

  //TODO: revisit later
  def findUserIds(groupId: Int) =
    userIdByGroupIdC(groupId).result

  def find(groupId: Int, userId: Int) =
    byPKC((groupId, userId)).result.headOption

  def delete(groupId: Int, userId: Int): FixedSqlAction[Int, NoStream, Write] =
    byPKC.applied((groupId, userId)).delete

  def makeAdmin(groupId: Int, userId: Int) =
    byPKC.applied((groupId, userId)).map(_.isAdmin).update(true)

  def dismissAdmin(groupId: Int, userId: Int) =
    byPKC.applied((groupId, userId)).map(_.isAdmin).update(false)

}
