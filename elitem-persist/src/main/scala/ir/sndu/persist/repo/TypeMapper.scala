package ir.sndu.persist.repo
import java.sql.Timestamp
import java.time.LocalDateTime

import slick.jdbc.PostgresProfile.api._

object TypeMapper {
  implicit val dateTimeColumnType = MappedColumnType.base[LocalDateTime, Timestamp](
    { localTime => Timestamp.valueOf(localTime) },
    { timestamp => timestamp.toLocalDateTime })
}
