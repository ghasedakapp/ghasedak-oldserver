package im.ghasedak.server.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile
import im.ghasedak.server.model.user.UserPhone
import slick.dbio.Effect.{ Read, Write }
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }

final class UserPhoneTable(tag: Tag) extends Table[UserPhone](tag, "user_phones") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
  def number = column[Long]("number")
  def title = column[String]("title")

  def * = (id, userId, number, title) <> (UserPhone.tupled, UserPhone.unapply)
}

object UserPhoneRepo {
  val phones = TableQuery[UserPhoneTable]

  private val byPhoneNumber = Compiled { number: Rep[Long] ⇒
    phones.filter(_.number === number)
  }

  private val phoneExists = Compiled { number: Rep[Long] ⇒
    phones.filter(_.number === number).exists
  }

  def exists(number: Long): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Read] = phoneExists(number).result

  // TODO: rename to findByNumber
  def findByPhoneNumber(number: Long): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Read] = byPhoneNumber(number).result

  def findByNumbers(numbers: Set[Long]): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Read] =
    phones.filter(_.number inSet numbers).result

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Read] =
    phones.filter(_.userId === userId).result

  def create(id: Int, userId: Int, number: Long, title: String): FixedSqlAction[Int, NoStream, Write] =
    phones += UserPhone(id, userId, number, title)
}
