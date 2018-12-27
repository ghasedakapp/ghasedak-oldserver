package ir.sndu.server.model.org

import java.time.LocalDateTime

final case class ApiKey(
  orgId:     Int,
  apiKey:    String,
  deletedAt: Option[LocalDateTime] = None)