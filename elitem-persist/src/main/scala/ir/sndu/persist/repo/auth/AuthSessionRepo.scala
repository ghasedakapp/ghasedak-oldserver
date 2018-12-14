package ir.sndu.persist.repo.auth

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthSession
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlAction

class AuthSessionTable(tag: Tag) extends Table[AuthSession](tag, "auth_sessions") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def tokenId = column[String]("token_id", O.PrimaryKey)

  def appId = column[Int]("app_id")

  def apiKey = column[String]("api_key")

  def deviceHash = column[String]("device_hash")

  def deviceInfo = column[String]("device_info")

  def sessionTime = column[LocalDateTime]("session_time")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (userId, tokenId, appId, apiKey, deviceHash, deviceInfo,
    sessionTime, deletedAt) <> ((AuthSession.apply _).tupled, AuthSession.unapply)
}

object AuthSessionRepo {

  private val sessions = TableQuery[AuthSessionTable]

  private val activeSessions = sessions.filter(_.deletedAt.isEmpty)

  def create(authSession: AuthSession): FixedSqlAction[Int, NoStream, Effect.Write] =
    sessions += authSession

}