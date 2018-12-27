package ir.sndu.server.model.auth

import java.time.LocalDateTime

final case class AuthToken(
  tokenId:   String,
  tokenKey:  String,
  deletedAt: Option[LocalDateTime])