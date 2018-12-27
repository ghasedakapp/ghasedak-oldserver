package ir.sndu.server.model.user

import java.time.LocalDateTime

final case class User(
  id:        Int,
  orgId:     Int,
  name:      String,
  createdAt: LocalDateTime,
  about:     Option[String]        = None,
  deletedAt: Option[LocalDateTime] = None)