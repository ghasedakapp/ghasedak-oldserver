package im.ghasedak.server.repo.user

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.repo.TypeMapper._
import im.ghasedak.server.repo.contact.UserContactRepo
import im.ghasedak.server.model.contact.UserContact
import im.ghasedak.server.model.user.{ User, UserAuth }
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

final class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Int]("id", O.PrimaryKey)

  def orgId = column[Int]("org_id")

  def name = column[String]("name")

  def createdAt = column[LocalDateTime]("created_at")

  def about = column[Option[String]]("about")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (id, orgId, name, createdAt, about, deletedAt) <> (User.tupled, User.unapply)

}

object UserRepo {

  val users = TableQuery[UserTable]

  val active = users.filter(_.deletedAt.isEmpty)

  def create(user: User): FixedSqlAction[Int, NoStream, Effect.Write] =
    users += user

  def find(id: Int): SqlAction[Option[User], NoStream, Effect.Read] =
    active.filter(_.id === id).result.headOption

  def find(ids: Set[Int]): FixedSqlStreamingAction[Seq[User], User, Effect.Read] =
    active.filter(_.id inSet ids).result

  def findUserContact(orgId: Int, ownerUserId: Int, userIds: Seq[Int]): FixedSqlStreamingAction[Seq[((User, Option[UserAuth]), Option[UserContact])], ((User, Option[UserAuth]), Option[UserContact]), Effect.Read] = {
    UserRepo.active
      .filter(_.orgId === orgId)
      .filter(_.id inSet userIds)
      .joinLeft(UserAuthRepo.active)
      .on(_.id === _.userId)
      .joinLeft(UserContactRepo.active filter (_.ownerUserId === ownerUserId) distinct)
      .on(_._1.id === _.contactUserId)
      .result
  }

  def isDeleted(userId: Int): DBIO[Boolean] =
    users.filter(_.id === userId).filter(_.deletedAt.nonEmpty).exists.result

  def findOrgId(userId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    active.filter(_.id === userId).map(_.orgId).result.headOption

}

