package im.ghasedak.server.rpc.contact

import im.ghasedak.api.user.{ ContactRecord, ContactType }
import im.ghasedak.server.repo.contact.{ UserEmailContactRepo, UserPhoneContactRepo }
import im.ghasedak.server.rpc.user.UserRpcErrors

trait ContactServiceHelper {
  this: ContactServiceImpl ⇒

  def getContactRecordUserId(contactRecord: ContactRecord, orgId: Int): Result[Int] = {
    for {
      //      _ ← fromBoolean(ContactRpcErrors.InvalidContactRecord)(contactRecord.`type` == im.ghasedak.api.user.ContactType.CONTACTTYPE_UNKNOWN)
      optUserId ← if (contactRecord.`type` == ContactType.CONTACTTYPE_PHONE)
        fromDBIO(UserPhoneContactRepo.findContactUserId(orgId, contactRecord.getLongValue).headOption)
      else if (contactRecord.`type` == ContactType.CONTACTTYPE_EMAIL)
        fromDBIO(UserEmailContactRepo.findContactUserId(orgId, contactRecord.getStringValue).headOption)
      else throw ContactRpcErrors.InvalidContactRecord
      userId ← fromOption(UserRpcErrors.UserNotFound)(optUserId)
    } yield userId
  }

}
