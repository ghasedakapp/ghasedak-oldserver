package im.ghasedak.server.user

import im.ghasedak.api.user.ContactRecord
import im.ghasedak.server.repo.contact.UserPhoneContactRepo
import im.ghasedak.server.repo.user.{ UserEmailRepo, UserPhoneRepo }
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.concurrent.ExecutionContext

object UserUtils {
  def getUserContactsRecord(userId: Int)(implicit ec: ExecutionContext): DBIOAction[Seq[ContactRecord], NoStream, Effect.Read with Effect.Read] = {
    for {
      phones ← UserPhoneRepo.findByUserId(userId)
      emails ← UserEmailRepo.findByUserId(userId)
    } yield phones.map(_.toRecord) ++ emails.map(_.toRecord)
  }
}
