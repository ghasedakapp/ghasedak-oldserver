package ir.sndu.server.model.org

import java.time.LocalDateTime

final case class ApiKey(
  orgId:     Int,
  apiKey:    String,
  title:     Option[String]        = None,
  deletedAt: Option[LocalDateTime] = None)