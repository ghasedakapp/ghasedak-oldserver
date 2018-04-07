package ir.sndu.server.rpc.auth

import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

import ir.sndu.persist.repo.UserRepo
import ir.sndu.server.auth.{ ApiSex, ApiUser }
import ir.sndu.server.model.user.{ User, UserState }
import slick.dbio.{ Effect, NoStream }

import scala.concurrent.Future

trait UserHandler {
  this: AuthServiceImpl =>
  import ir.sndu.server.rpc.ApiConversions._
  def create(numbrt: Long, name: String, sex: ApiSex): slick.dbio.DBIOAction[ApiUser, NoStream, Effect.Write] = {
    val id = ThreadLocalRandom.current().nextInt()
    UserRepo.create(User(id, "", name, "IR", sex, UserState.Registered, LocalDateTime.now())) map (_ => ApiUser(id, name, "", sex))
  }
}
