package ir.sndu.server.model.auth

import java.time.LocalDateTime

import com.typesafe.config.{ Config, ConfigFactory }
import ir.sndu.server.model.org.ApiKey

import scala.collection.JavaConverters._

object AuthSession {

  val config: Config = ConfigFactory.load()

  private val officialApiKeys: Seq[ApiKey] =
    config.getConfigList("module.auth.official-api-keys")
      .asScala.map { conf ⇒
        ApiKey(
          conf.getInt("org-id"),
          conf.getString("api-key"))
      }

  def isOfficialApiKey(apiKey: String): Option[ApiKey] =
    officialApiKeys.find(_.apiKey == apiKey)

}

final case class AuthSession(
  orgId:     Int,
  apiKey:    String,
  userId:    Int,
  tokenId:   String,
  createdAt: LocalDateTime,
  deletedAt: Option[LocalDateTime] = None)
