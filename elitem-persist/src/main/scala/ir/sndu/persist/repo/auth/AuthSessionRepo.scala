package ir.sndu.persist.repo.auth

import java.time.{ LocalDateTime, ZoneOffset }

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthSession
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlAction

class AuthSessionTable(tag: Tag) extends Table[AuthSession](tag, "auth_sessions") {

  def orgId = column[Int]("org_id", O.PrimaryKey)

  def apiKey = column[String]("api_key")

  def userId = column[Int]("user_id", O.PrimaryKey)

  def tokenId = column[String]("token_id", O.PrimaryKey)

  def createdAt = column[LocalDateTime]("created_at")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (orgId, apiKey, userId, tokenId, createdAt, deletedAt) <> ((AuthSession.apply _).tupled, AuthSession.unapply)

}

object AuthSessionRepo {

  val sessions = TableQuery[AuthSessionTable]

  def create(authSession: AuthSession): FixedSqlAction[Int, NoStream, Effect.Write] =
    sessions += authSession

  def delete(tokenId: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    sessions.filter(_.tokenId === tokenId).map(_.deletedAt)
      .update(Some(LocalDateTime.now(ZoneOffset.UTC)))

}