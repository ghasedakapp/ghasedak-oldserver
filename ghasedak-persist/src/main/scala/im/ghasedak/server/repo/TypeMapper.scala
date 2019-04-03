package im.ghasedak.server.repo

import java.sql.Timestamp
import java.time.{ Instant, LocalDateTime }

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object TypeMapper {

  implicit val dateTimeColumnType: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    { localTime ⇒ Timestamp.valueOf(localTime) },
    { timestamp ⇒ timestamp.toLocalDateTime })

  implicit val instantColumnType: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, Timestamp](
    { instant ⇒ Timestamp.from(instant) },
    { timestamp ⇒ timestamp.toInstant })

}
