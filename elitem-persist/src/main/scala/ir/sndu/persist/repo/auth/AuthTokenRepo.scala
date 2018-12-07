package ir.sndu.persist.repo.auth

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthToken
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

class AuthTokenTable(tag: Tag) extends Table[AuthToken](tag, "auth_tokens") {

  def tokenId = column[String]("token_id", O.PrimaryKey)

  def tokenKy = column[String]("token_key")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (tokenId, tokenKy, deletedAt) <> (AuthToken.tupled, AuthToken.unapply)

}

object AuthTokenRepo {

  val tokens = TableQuery[AuthTokenTable]

  val activeTokens = tokens.filter(_.deletedAt.isEmpty)

  def create(token: AuthToken): FixedSqlAction[Int, NoStream, Effect.Write] =
    tokens += token

  def find(tokenId: String): SqlAction[Option[String], NoStream, Effect.Read] =
    activeTokens.filter(_.tokenId === tokenId).map(_.tokenKy).result.headOption

}