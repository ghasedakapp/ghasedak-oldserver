package ir.sndu.server.model.auth

import java.time.LocalDateTime

@SerialVersionUID(1L)
case class Token(tokenId: String, tokenKey: String, deletedAt: Option[LocalDateTime])