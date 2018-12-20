package ir.sndu.persist.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.user.UserEmail
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.SqlAction

final class UserEmailTable(tag: Tag) extends Table[UserEmail](tag, "user_emails") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def email = column[String]("email")

  def * = (userId, email) <> (UserEmail.tupled, UserEmail.unapply)

}

object UserEmailRepo {

  private val emails = TableQuery[UserEmailTable]

  def findEmail(userId: Int): SqlAction[Option[String], NoStream, Effect.Read] =
    emails.filter(_.userId === userId).map(_.email).result.headOption

}
