package ir.sndu.server.utils

import java.time.{ LocalDateTime, ZoneOffset }

object DateTimeHelper {
  def origin: LocalDateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)

}
