package ir.sndu.persist.repo.group

import ir.sndu.server.apigroup.ApiGroupType
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object GroupTypeColumnType {
  implicit val groupTypeColumnType: JdbcType[ApiGroupType] with BaseTypedType[ApiGroupType] =
    MappedColumnType.base[ApiGroupType, Int](_.value, ApiGroupType.fromValue)
}
