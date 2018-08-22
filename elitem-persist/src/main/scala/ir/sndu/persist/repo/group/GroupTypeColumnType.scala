package ir.sndu.persist.repo.group

import ir.sndu.server.groups.ApiGroupType
import slick.jdbc.PostgresProfile.api._

object GroupTypeColumnType {
  implicit val groupTypeColumnType =
    MappedColumnType.base[ApiGroupType, Int](_.value, ApiGroupType.fromValue)
}
