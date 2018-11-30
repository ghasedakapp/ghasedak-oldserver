package ir.sndu.persist.repo.user

import ir.sndu.server.model.user.Sex
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object SexColumnType {
  implicit val sexColumnType: JdbcType[Sex] with BaseTypedType[Sex] =
    MappedColumnType.base[Sex, Int](_.toInt, Sex.fromInt)
}
