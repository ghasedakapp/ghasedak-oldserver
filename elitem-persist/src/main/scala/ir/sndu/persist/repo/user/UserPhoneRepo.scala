package ir.sndu.persist.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.user.UserPhone
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class UserPhoneTable(tag: Tag) extends Table[UserPhone](tag, "user_phones") {

  def orgId = column[Int]("org_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def phoneNumber = column[Long]("phone_number")

  def * = (orgId, userId, phoneNumber) <> (UserPhone.tupled, UserPhone.unapply)

}

object UserPhoneRepo {

  val phones = TableQuery[UserPhoneTable]

  def create(userPhone: UserPhone): FixedSqlAction[Int, NoStream, Effect.Write] =
    phones += userPhone

  def findByPhoneNumberAndOrgId(phoneNumber: Long, orgId: Int): SqlAction[Option[UserPhone], NoStream, Effect.Read] =
    phones.filter(_.orgId === orgId).filter(_.phoneNumber === phoneNumber).result.headOption

  def findUserIdByPhoneNumberAndOrgId(phoneNumber: Long, orgId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    phones.filter(_.orgId === orgId).filter(_.phoneNumber === phoneNumber).map(_.userId).result.headOption

  def findPhoneNumberByUserId(userId: Int): SqlAction[Option[Long], NoStream, Effect.Read] =
    phones.filter(_.userId === userId).map(_.phoneNumber).result.headOption

}
