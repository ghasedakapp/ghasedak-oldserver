package ir.sndu.server.user

import im.ghasedak.api.contact.{ ApiContactInfo, ApiContactType }
import ir.sndu.persist.repo.user.{ UserEmailRepo, UserPhoneRepo }
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

object UserUtils {

  def getUserContactInfo(userId: Int)(implicit ec: ExecutionContext): DBIOAction[Seq[ApiContactInfo], NoStream, Effect.Read with Effect.Read] = {
    for {
      phoneNumber ← UserPhoneRepo.findNumber(userId)
      email ← UserEmailRepo.findEmail(userId)
    } yield {
      var contactsInfo = ArrayBuffer.empty[ApiContactInfo]
      if (phoneNumber.isDefined)
        contactsInfo += ApiContactInfo(
          contactType = ApiContactType.PHONE,
          phoneNumber = phoneNumber)
      if (email.isDefined)
        contactsInfo += ApiContactInfo(
          contactType = ApiContactType.EMAIL,
          email = email)
      contactsInfo
    }
  }

}
