package ir.sndu.persist.repo

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object TypeMapper {

  implicit val dateTimeColumnType: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    { localTime ⇒ Timestamp.valueOf(localTime) },
    { timestamp ⇒ timestamp.toLocalDateTime })

}
