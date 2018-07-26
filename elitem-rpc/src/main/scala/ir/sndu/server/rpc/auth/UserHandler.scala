package ir.sndu.server.rpc.auth

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

import ir.sndu.persist.repo.AuthIdRepo
import ir.sndu.persist.repo.user.{ UserPhoneRepo, UserRepo }
import ir.sndu.server.model.user.{ User, UserState }
import ir.sndu.server.users.{ ApiSex, ApiUser }
import slick.dbio.{ Effect, NoStream }

trait UserHandler {
  this: AuthServiceImpl ⇒

  import ir.sndu.server.rpc.ApiConversions._

  def createUser(number: Long, name: String, sex: ApiSex): slick.dbio.DBIOAction[ApiUser, NoStream, Effect.Write] = {
    val userId = ThreadLocalRandom.current().nextInt()
    val userPhoneId = ThreadLocalRandom.current().nextInt()
    for {
      _ ← UserRepo.create(User(userId, "", name, "IR", sex, UserState.Registered, LocalDateTime.now()))
      _ ← UserPhoneRepo.create(userPhoneId, userId, "", number, name)
    } yield ApiUser(userId, name, "", sex)
  }

  def createToken(userId: Int): slick.dbio.DBIOAction[String, NoStream, Effect.Write] = {
    val token = UUID.randomUUID().toString
    AuthIdRepo.create(token, Some(userId), None) map (_ ⇒ token)
  }

}
