package ir.sndu.server.user

import im.ghasedak.api.contact.ApiContactRecord
import ir.sndu.persist.repo.user.{ UserEmailRepo, UserPhoneRepo }
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

object UserUtils {

  def getUserContactsRecord(userId: Int)(implicit ec: ExecutionContext): DBIOAction[Seq[ApiContactRecord], NoStream, Effect.Read with Effect.Read] = {
    for {
      phoneNumber ← UserPhoneRepo.findPhoneNumber(userId)
      email ← UserEmailRepo.findEmail(userId)
    } yield {
      var contactsRecord = ArrayBuffer.empty[ApiContactRecord]
      if (phoneNumber.isDefined)
        contactsRecord += ApiContactRecord()
          .withPhoneNumber(phoneNumber.get)
      if (email.isDefined)
        contactsRecord += ApiContactRecord()
          .withEmail(email.get)
      contactsRecord
    }
  }

}
