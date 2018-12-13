package ir.sndu.persist.repo.user

import ir.sndu.server.model.user.UserPhone
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import slick.sql.FixedSqlStreamingAction

final class UserPhoneTable(tag: Tag) extends Table[UserPhone](tag, "user_phones") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def number = column[Long]("number")

  def * = (userId, number) <> (UserPhone.tupled, UserPhone.unapply)

}

object UserPhoneRepo {

  private val phones = TableQuery[UserPhoneTable]

  private val byPhoneNumber = Compiled { number: Rep[Long] ⇒
    phones.filter(_.number === number)
  }

  def findByPhoneNumber(number: Long): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Effect.Read] =
    byPhoneNumber(number).result

}
