package ir.sndu.persist.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.user.UserPhone
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }

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

  def create(userPhone: UserPhone): FixedSqlAction[Int, NoStream, Effect.Write] =
    phones += userPhone

  def findByPhoneNumber(number: Long): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Effect.Read] =
    byPhoneNumber(number).result

}
