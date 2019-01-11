package im.ghasedak.server.repo.org

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.repo.TypeMapper._
import im.ghasedak.server.model.org.ApiKey
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.SqlAction

final class ApiKeyTable(tag: Tag) extends Table[ApiKey](tag, "api_keys") {

  def orgId = column[Int]("org_id", O.PrimaryKey)

  def apiKey = column[String]("api_key", O.PrimaryKey, O.Unique)

  def title = column[Option[String]]("title")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (orgId, apiKey, title, deletedAt) <> (ApiKey.tupled, ApiKey.unapply)

}

object ApiKeyRepo {

  val apiKeys = TableQuery[ApiKeyTable]

  val active = apiKeys.filter(_.deletedAt.isEmpty)

  def find(apiKey: String): SqlAction[Option[ApiKey], NoStream, Effect.Read] =
    active.filter(_.apiKey === apiKey).result.headOption

}
