package im.ghasedak.server.repo.user

import im.ghasedak.api.user.Sex
import com.github.tminglei.slickpg.ExPostgresProfile.api._

object SexColumnType {
  implicit val sexColumnType =
    MappedColumnType.base[Sex, Int](_.value, Sex.fromValue)
}
