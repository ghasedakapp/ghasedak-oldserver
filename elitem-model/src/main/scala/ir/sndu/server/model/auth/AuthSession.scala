package ir.sndu.server.model.auth

import java.time.LocalDateTime

object AuthSession {

  private val apiKeys = Seq(
    "4b654ds5b4654sd65b44s6d5b46s5d4b" // official
  )

  def isValidApiKey(apiKey: String): Boolean = apiKeys.contains(apiKey)

  def getAppTitle(appId: Int): String = appId match {
    case 1  ⇒ "Android"
    case 2  ⇒ "IOS"
    case 3  ⇒ "Web"
    case 42 ⇒ "Test"
    case _  ⇒ "Unknown"
  }

}

@SerialVersionUID(1L)
final case class AuthSession(
  userId:      Int,
  tokenId:     String,
  appId:       Int,
  apiKey:      String,
  deviceHash:  String,
  deviceInfo:  String,
  sessionTime: LocalDateTime,
  deletedAt:   Option[LocalDateTime] = None)
