package ir.sndu.persist.repo

import ir.sndu.server.model.user.UserState
import slick.jdbc.PostgresProfile.api._

object UserStateColumnType {
  implicit val userStateColumnType =
    MappedColumnType.base[UserState, Int](_.toInt, UserState.fromInt)
}
