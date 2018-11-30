package ir.sndu.persist.repo.user

import ir.sndu.server.model.user.UserState
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object UserStateColumnType {
  implicit val userStateColumnType: JdbcType[UserState] with BaseTypedType[UserState] =
    MappedColumnType.base[UserState, Int](_.toInt, UserState.fromInt)
}
