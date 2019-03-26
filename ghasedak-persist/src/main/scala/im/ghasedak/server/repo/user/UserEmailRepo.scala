package im.ghasedak.server.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.user.UserEmail
import slick.dbio.Effect.{ Read, Write }
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

final class UserEmailTable(tag: Tag) extends Table[UserEmail](tag, "user_emails") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
  def email = column[String]("email")
  def title = column[String]("title")

  def * = (id, userId, email, title) <> (UserEmail.tupled, UserEmail.unapply)
}

object UserEmailRepo {
  val emails = TableQuery[UserEmailTable]

  val byEmail = Compiled { email: Rep[String] ⇒
    emails.filter(_.email === email)
  }

  val emailExists = Compiled { email: Rep[String] ⇒
    emails.filter(_.email === email).exists
  }

  def findByEmails(emailSet: Set[String]): FixedSqlStreamingAction[Seq[UserEmail], UserEmail, Read] =
    emails.filter(_.email inSet emailSet).result

  def find(email: String): SqlAction[Option[UserEmail], NoStream, Read] =
    byEmail(email).result.headOption

  def findByDomain(domain: String): FixedSqlStreamingAction[Seq[UserEmail], UserEmail, Read] =
    emails.filter(_.email.like(s"%@$domain")).result

  def exists(email: String): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Read] =
    emailExists(email).result

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[UserEmail], UserEmail, Read] =
    emails.filter(_.userId === userId).result

  def findByUserIds(userIds: Set[Int]): FixedSqlStreamingAction[Seq[UserEmail], UserEmail, Read] =
    emails.filter(_.userId inSet userIds).result

  def create(id: Int, userId: Int, email: String, title: String): FixedSqlAction[Int, NoStream, Write] =
    emails += UserEmail(id, userId, email.toLowerCase, title)
}
