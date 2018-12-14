package ir.sndu.server.model.auth

import java.time.LocalDateTime

@SerialVersionUID(1L)
final case class AuthToken(tokenId: String, tokenKey: String, deletedAt: Option[LocalDateTime])