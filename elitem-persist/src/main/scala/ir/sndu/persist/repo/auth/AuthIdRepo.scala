package ir.sndu.persist.repo

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

final class AuthIdTable(tag: Tag) extends Table[AuthId](tag, "auth_ids") {
  def id = column[String]("id", O.PrimaryKey)

  def userId = column[Option[Int]]("user_id")

  def publicKeyHash = column[Option[Long]]("public_key_hash")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (id, userId, publicKeyHash) <> (AuthId.tupled, AuthId.unapply)
}

object AuthIdRepo {
  private val authIds = TableQuery[AuthIdTable]

  private val activeAuthIds = authIds.filter(_.deletedAt.isEmpty)
  private val activeAuthIdsCompiled = Compiled(activeAuthIds)

  def create(authId: String, userId: Option[Int], publicKeyHash: Option[Long]) =
    authIds += AuthId(authId, userId, publicKeyHash)

  def create(authId: String) = authIds += AuthId(authId, None, None)

  def byAuthIdNotDeleted(authId: Rep[String]) =
    activeAuthIds.filter(a ⇒ a.id === authId)

  val byAuthIdNotDeletedCompiled = Compiled(byAuthIdNotDeleted _)

  val userIdByAuthIdNotDeletedCompiled = Compiled(
    (authId: Rep[String]) ⇒
      byAuthIdNotDeleted(authId).map(_.userId))

  def activeByUserId(userId: Rep[Int]) = activeAuthIds.filter(_.userId === userId)

  val activeByUserIdCompiled = Compiled((userId: Rep[Int]) ⇒ activeByUserId(userId))
  val activeIdByUserIdCompiled = Compiled((userId: Rep[Int]) ⇒ activeByUserId(userId) map (_.id))
  val firstActiveIdByUserIdCompiled = Compiled((userId: Rep[Int]) ⇒ activeByUserId(userId) map (_.id) take (1))

  def activeIdByUserIds(userIds: Set[Int]) = activeAuthIds.filter(_.userId inSetBind userIds).map(_.id)

  def setUserData(authId: Long, userId: Int) =
    sqlu"UPDATE auth_ids SET user_id = $userId WHERE id = $authId AND deleted_at IS NULL"

  def find(authId: String) =
    byAuthIdNotDeletedCompiled(authId).result.headOption

  def findUserId(authId: String)(implicit ec: ExecutionContext) =
    userIdByAuthIdNotDeletedCompiled(authId).result.headOption map (_.flatten)

  def findByUserId(userId: Int) =
    activeByUserIdCompiled(userId).result

  def findIdByUserId(userId: Int) =
    activeIdByUserIdCompiled(userId).result

  def findFirstIdByUserId(userId: Int) =
    firstActiveIdByUserIdCompiled(userId).result.headOption

  def findIdByUserIds(userIds: Set[Int]) =
    activeIdByUserIds(userIds).result

  def delete(id: String) =
    activeAuthIds.filter(_.id === id).map(_.deletedAt).update(Some(LocalDateTime.now()))

  def deleteByUser(userId: Int) =
    activeAuthIds.filter(_.userId === userId).map(_.deletedAt).update(Some(LocalDateTime.now()))
}
