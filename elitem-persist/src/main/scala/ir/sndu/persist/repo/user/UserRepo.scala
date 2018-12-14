package ir.sndu.persist.repo.user

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.user.User
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlStreamingAction

final class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Int]("id", O.PrimaryKey)

  def name = column[String]("name")

  def countryCode = column[String]("country_code")

  def createdAt = column[LocalDateTime]("created_at")

  def nickname = column[Option[String]]("nickname")

  def about = column[Option[String]]("about")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (id, name, countryCode, createdAt, nickname, about, deletedAt) <> (User.tupled, User.unapply)

}

object UserRepo {

  private val users = TableQuery[UserTable]

  private val activeUsers = users.filter(_.deletedAt.nonEmpty)

  def find(id: Int): FixedSqlStreamingAction[Seq[User], User, Effect.Read] =
    activeUsers.filter(_.id === id).result

  def isDeleted(userId: Int): DBIO[Boolean] =
    users.filter(_.id === userId).filter(_.deletedAt.nonEmpty).exists.result

}

