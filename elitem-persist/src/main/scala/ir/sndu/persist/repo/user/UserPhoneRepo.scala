package ir.sndu.persist.repo.user

import ir.sndu.server.model.user.UserPhone
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class UserPhoneTable(tag: Tag) extends Table[UserPhone](tag, "user_phones") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def id = column[Int]("id", O.PrimaryKey)

  def accessSalt = column[String]("access_salt")

  def number = column[Long]("number")

  def * = (id, userId, accessSalt, number) <> (UserPhone.tupled, UserPhone.unapply)

}

object UserPhoneRepo {

  val phones = TableQuery[UserPhoneTable]

}
