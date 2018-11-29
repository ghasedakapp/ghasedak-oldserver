package ir.sndu.persist.repo.auth

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.Token
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class TokenTable(tag: Tag) extends Table[Token](tag, "tokens") {

  def tokenId = column[String]("token_id", O.PrimaryKey)

  def tokenKy = column[String]("token_key")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (tokenId, tokenKy, deletedAt) <> (Token.tupled, Token.unapply)
}

object TokenRepo {

  private val tokens = TableQuery[TokenTable]

  private val activeTokens = tokens.filter(_.deletedAt.isEmpty)

  def create(token: Token): FixedSqlAction[Int, NoStream, Effect.Write] =
    tokens += token

  def find(tokenId: String): SqlAction[Option[String], NoStream, Effect.Read] =
    activeTokens.filter(_.tokenId === tokenId).map(_.tokenKy).result.headOption

}