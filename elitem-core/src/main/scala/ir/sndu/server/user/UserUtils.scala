package ir.sndu.server.user

import im.ghasedak.api.contact.{ ApiContact, ApiContactType }
import ir.sndu.persist.repo.user.{ UserEmailRepo, UserPhoneRepo }
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.concurrent.ExecutionContext

object UserUtils {

  def getUserContactInfo(userId: Int)(implicit ec: ExecutionContext): DBIOAction[ApiContact, NoStream, Effect.Read with Effect.Read] = {
    for {
      number ← UserPhoneRepo.findNumber(userId)
      email ← UserEmailRepo.findEmail(userId)
      contactType = if (number.isDefined && email.isDefined) ApiContactType.ApiContactType_BOTH
      else if (number.isDefined) ApiContactType.ApiContactType_PHONE
      else if (email.isDefined) ApiContactType.ApiContactType_EMAIL
      else ApiContactType.ApiContactType_UNKNOWN
    } yield ApiContact(contactType, number, email)
  }

}
