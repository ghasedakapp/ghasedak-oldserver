package im.ghasedak.server.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile
import im.ghasedak.server.model.user.UserPhone
import slick.dbio.Effect.{ Read, Write }
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

final class UserPhoneTable(tag: Tag) extends Table[UserPhone](tag, "user_phones") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
  def orgId = column[Int]("org_id")
  def number = column[Long]("number")
  def title = column[String]("title")

  def * = (id, userId, orgId, number, title) <> (UserPhone.tupled, UserPhone.unapply)
}

object UserPhoneRepo {
  val phones = TableQuery[UserPhoneTable]

  private def byPhoneNumber(number: Rep[Long], orgId: Rep[Int]) =
    phones.filter(p ⇒ p.number === number && p.orgId === orgId)

  private val phoneExists = Compiled { number: Rep[Long] ⇒
    phones.filter(_.number === number).exists
  }

  def exists(number: Long): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Read] = phoneExists(number).result

  def findUserIdByNumber(orgId: Int, number: Long): SqlAction[Option[Int], NoStream, Read] =
    byPhoneNumber(number, orgId).map(_.userId).result.headOption

  def findByNumber(orgId: Int, number: Long): SqlAction[Option[UserPhone], NoStream, Read] =
    byPhoneNumber(number, orgId).result.headOption

  def findByNumbers(numbers: Set[Long], orgId: Int): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Read] =
    phones.filter(_.number inSet numbers).filter(_.orgId === orgId).result

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[UserPhone], UserPhone, Read] =
    phones.filter(_.userId === userId).result

  def create(id: Int, userId: Int, orgId: Int, number: Long, title: String): FixedSqlAction[Int, NoStream, Write] =
    phones += UserPhone(id, userId, orgId, number, title)

  def create(phone: UserPhone): FixedSqlAction[Int, NoStream, Write] =
    phones += phone
}
