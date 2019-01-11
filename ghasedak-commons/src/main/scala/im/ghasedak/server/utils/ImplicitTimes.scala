package im.ghasedak.server.utils

import java.time.{ LocalDateTime, ZoneOffset }

object ImplicitTimes {
  implicit class RichLocalDateTime(time: LocalDateTime) {
    def ORIGIN: LocalDateTime = {
      LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
    }
  }
}
