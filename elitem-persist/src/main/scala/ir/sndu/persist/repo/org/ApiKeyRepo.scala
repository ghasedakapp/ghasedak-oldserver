package ir.sndu.persist.repo.org

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.org.ApiKey
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.SqlAction

final class ApiKeyTable(tag: Tag) extends Table[ApiKey](tag, "api_keys") {

  def orgId = column[Int]("org_id", O.PrimaryKey)

  def apiKey = column[String]("api_key", O.PrimaryKey, O.Unique)

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (orgId, apiKey, deletedAt) <> (ApiKey.tupled, ApiKey.unapply)

}

object ApiKeyRepo {

  private val apiKeys = TableQuery[ApiKeyTable]

  private val active = apiKeys.filter(_.deletedAt.isEmpty)

  def find(apiKey: String): SqlAction[Option[ApiKey], NoStream, Effect.Read] =
    active.filter(_.apiKey === apiKey).result.headOption

}
