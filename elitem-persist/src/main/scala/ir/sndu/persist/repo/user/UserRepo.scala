package ir.sndu.persist.repo.user

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.user.User
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Int]("id", O.PrimaryKey)

  def accessSalt = column[String]("access_salt")

  def name = column[String]("name")

  def countryCode = column[String]("country_code")

  def createdAt = column[LocalDateTime]("created_at")

  def nickname = column[Option[String]]("nickname")

  def about = column[Option[String]]("about")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (id, accessSalt, name, countryCode, createdAt, nickname, about, deletedAt) <> (User.tupled, User.unapply)

}

object UserRepo {

  val users = TableQuery[UserTable]

  val activeUsers = users.filter(_.deletedAt.nonEmpty)

  def isDeleted(userId: Int): DBIO[Boolean] =
    users.filter(_.id === userId).filter(_.deletedAt.nonEmpty).exists.result

}

