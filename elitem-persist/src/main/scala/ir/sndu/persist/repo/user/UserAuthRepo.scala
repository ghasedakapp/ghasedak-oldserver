package ir.sndu.persist.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.user.UserAuth
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class UserAuthTable(tag: Tag) extends Table[UserAuth](tag, "users_auth") {

  def orgId = column[Int]("org_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def phoneNumber = column[Option[Long]]("phone_number")

  def email = column[Option[String]]("email")

  def nickname = column[Option[String]]("nickname")

  def countryCode = column[Option[String]]("country_code")

  def isDeleted = column[Boolean]("is_deleted")

  override def * = (orgId, userId, phoneNumber, email, nickname, countryCode, isDeleted) <> (UserAuth.tupled, UserAuth.unapply)

}

object UserAuthRepo {

  val usersAuth = TableQuery[UserAuthTable]

  val active = usersAuth.filter(_.isDeleted === false)

  def create(userAuth: UserAuth): FixedSqlAction[Int, NoStream, Effect.Write] =
    usersAuth += userAuth

  def findByPhoneNumberAndOrgId(phoneNumber: Long, orgId: Int): SqlAction[Option[UserAuth], NoStream, Effect.Read] =
    active.filter(_.orgId === orgId).filter(_.phoneNumber === phoneNumber).result.headOption

  def findUserIdByPhoneNumberAndOrgId(phoneNumber: Long, orgId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    active.filter(_.orgId === orgId).filter(_.phoneNumber === phoneNumber).map(_.userId).result.headOption

  def findPhoneNumberByUserId(userId: Int): SqlAction[Option[Option[Long]], NoStream, Effect.Read] =
    active.filter(_.userId === userId).map(_.phoneNumber).result.headOption

  def findNicknameByUserId(userId: Int): SqlAction[Option[Option[String]], NoStream, Effect.Read] =
    active.filter(_.userId === userId).map(_.nickname).result.headOption

  def findEmailByUserId(userId: Int): SqlAction[Option[Option[String]], NoStream, Effect.Read] =
    active.filter(_.userId === userId).map(_.email).result.headOption

  def findByEmailAndOrgId(email: String, orgId: Int): SqlAction[Option[UserAuth], NoStream, Effect.Read] =
    active.filter(_.orgId === orgId).filter(_.email === email).result.headOption

  def findUserIdByEmailAndOrgId(email: String, orgId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    active.filter(_.orgId === orgId).filter(_.email === email).map(_.userId).result.headOption

}
