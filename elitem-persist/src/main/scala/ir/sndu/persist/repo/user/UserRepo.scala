package ir.sndu.persist.repo.user

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.user.User
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Int]("id", O.PrimaryKey)

  def orgId = column[Int]("org_id")

  def name = column[String]("name")

  def createdAt = column[LocalDateTime]("created_at")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (id, orgId, name, createdAt, deletedAt) <> (User.tupled, User.unapply)

}

object UserRepo {

  private val users = TableQuery[UserTable]

  private val activeUsers = users.filter(_.deletedAt.isEmpty)

  def create(user: User): FixedSqlAction[Int, NoStream, Effect.Write] =
    users += user

  def find(id: Int): SqlAction[Option[User], NoStream, Effect.Read] =
    activeUsers.filter(_.id === id).result.headOption

  def isDeleted(userId: Int): DBIO[Boolean] =
    users.filter(_.id === userId).filter(_.deletedAt.nonEmpty).exists.result

  def findOrgId(userId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    activeUsers.filter(_.id === userId).map(_.orgId).result.headOption

}

