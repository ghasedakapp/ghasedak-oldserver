package ir.sndu.persist.repo.user

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.user.UserInfo
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class UserInfoTable(tag: Tag) extends Table[UserInfo](tag, "users_info") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def countryCode = column[Option[String]]("country_code")

  def nickname = column[Option[String]]("nickname")

  def about = column[Option[String]]("about")

  def * = (userId, countryCode, nickname, about) <> (UserInfo.tupled, UserInfo.unapply)

}

object UserInfoRepo {

  val usersInfo = TableQuery[UserInfoTable]

  def create(userInfo: UserInfo): FixedSqlAction[Int, NoStream, Effect.Write] =
    usersInfo += userInfo

  def find(userId: Int): SqlAction[Option[UserInfo], NoStream, Effect.Read] =
    usersInfo.filter(_.userId === userId).result.headOption

}

