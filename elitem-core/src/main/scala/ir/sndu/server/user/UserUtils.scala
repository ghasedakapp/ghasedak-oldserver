package ir.sndu.server.user

import im.ghasedak.api.contact.ApiContactInfo
import ir.sndu.persist.repo.user.{ UserEmailRepo, UserPhoneRepo }
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.concurrent.ExecutionContext

object UserUtils {

  def getUserContactInfo(userId: Int)(implicit ec: ExecutionContext): DBIOAction[ApiContactInfo, NoStream, Effect.Read with Effect.Read] = {
    for {
      phoneNumber ← UserPhoneRepo.findNumber(userId)
      email ← UserEmailRepo.findEmail(userId)
    } yield ApiContactInfo(phoneNumber, email)
  }

}
