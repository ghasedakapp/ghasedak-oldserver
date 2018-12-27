package ir.sndu.persist.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.user.UserEmail
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.SqlAction

final class UserEmailTable(tag: Tag) extends Table[UserEmail](tag, "user_emails") {

  def orgId = column[Int]("org_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def email = column[String]("email")

  def * = (orgId, userId, email) <> (UserEmail.tupled, UserEmail.unapply)

}

object UserEmailRepo {

  val emails = TableQuery[UserEmailTable]

  def findEmailByUserId(userId: Int): SqlAction[Option[String], NoStream, Effect.Read] =
    emails.filter(_.userId === userId).map(_.email).result.headOption

  def findByEmailAndOrgId(email: String, orgId: Int): SqlAction[Option[UserEmail], NoStream, Effect.Read] =
    emails.filter(_.orgId === orgId).filter(_.email === email).result.headOption

  def findUserIdByEmailAndOrgId(email: String, orgId: Int): SqlAction[Option[Int], NoStream, Effect.Read] =
    emails.filter(_.orgId === orgId).filter(_.email === email).map(_.userId).result.headOption

}
