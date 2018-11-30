package ir.sndu.persist.repo.auth

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthSession
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

final class AuthSessionTable(tag: Tag) extends Table[AuthSession](tag, "auth_sessions") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def tokenId = column[String]("token_id", O.PrimaryKey)

  def appId = column[Int]("app_id")

  def apiKey = column[String]("api_key")

  def deviceHash = column[String]("device_hash")

  def deviceInfo = column[String]("device_info")

  def sessionTime = column[LocalDateTime]("session_time")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (userId, tokenId, appId, apiKey, deviceHash, deviceInfo,
    sessionTime, deletedAt) <> ((AuthSession.apply _).tupled, AuthSession.unapply)
}

object AuthSessionRepo {

  val sessions = TableQuery[AuthSessionTable]

  val activeSessions = sessions.filter(_.deletedAt.isEmpty)

}