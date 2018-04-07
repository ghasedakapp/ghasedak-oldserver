package ir.sndu.persist.repo

import ir.sndu.server.model.user.Sex
import slick.jdbc.PostgresProfile.api._

object SexColumnType {
  implicit val sexColumnType =
    MappedColumnType.base[Sex, Int](_.toInt, Sex.fromInt)
}
