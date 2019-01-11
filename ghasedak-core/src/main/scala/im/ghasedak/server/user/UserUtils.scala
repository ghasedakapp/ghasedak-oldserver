package im.ghasedak.server.user

import im.ghasedak.api.contact.ApiContactRecord
import im.ghasedak.server.repo.user.UserAuthRepo
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

object UserUtils {

  def getUserContactsRecord(userId: Int)(implicit ec: ExecutionContext): DBIOAction[Seq[ApiContactRecord], NoStream, Effect.Read with Effect.Read] = {
    for {
      phoneNumber ← UserAuthRepo.findPhoneNumberByUserId(userId).map(_.flatten)
      email ← UserAuthRepo.findEmailByUserId(userId).map(_.flatten)
      nickname ← UserAuthRepo.findNicknameByUserId(userId).map(_.flatten)
    } yield {
      var contactsRecord = ArrayBuffer.empty[ApiContactRecord]
      if (phoneNumber.isDefined)
        contactsRecord += ApiContactRecord()
          .withPhoneNumber(phoneNumber.get)
      if (email.isDefined)
        contactsRecord += ApiContactRecord()
          .withEmail(email.get)
      if (nickname.isDefined)
        contactsRecord += ApiContactRecord()
          .withNickname(nickname.get)
      contactsRecord
    }
  }

}
