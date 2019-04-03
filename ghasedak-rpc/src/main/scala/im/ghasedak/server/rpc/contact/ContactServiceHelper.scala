package im.ghasedak.server.rpc.contact

import im.ghasedak.api.user.{ ContactRecord, ContactType }
import im.ghasedak.server.model.contact.{ UserEmailContact, UserPhoneContact }
import im.ghasedak.server.repo.contact.{ UserEmailContactRepo, UserPhoneContactRepo }
import im.ghasedak.server.repo.user.{ UserEmailRepo, UserPhoneRepo }
import im.ghasedak.server.rpc.user.UserRpcErrors

trait ContactServiceHelper {
  this: ContactServiceImpl ⇒

  def getContactRecordUserId(contactRecord: ContactRecord, orgId: Int): Result[Int] = {
    for {
      //      _ ← fromBoolean(ContactRpcErrors.InvalidContactRecord)(contactRecord.`type` == im.ghasedak.api.user.ContactType.CONTACTTYPE_UNKNOWN)
      optUserId ← if (contactRecord.`type` == ContactType.CONTACTTYPE_PHONE)
        fromDBIO(UserPhoneRepo.findUserIdByNumber(orgId, contactRecord.getLongValue))
      else if (contactRecord.`type` == ContactType.CONTACTTYPE_EMAIL)
        fromDBIO(UserEmailRepo.findUserId(orgId, contactRecord.getStringValue))
      else throw ContactRpcErrors.InvalidContactRecord
      userId ← fromOption(UserRpcErrors.UserNotFound)(optUserId)
    } yield userId
  }

  protected def addPhoneContact(clientUserId: Int, phone: UserPhoneContact) = {
    for {
      _ ← fromDBIOBoolean(ContactRpcErrors.ContactAlreadyExists)(UserPhoneContactRepo.exist(phone.orgId, clientUserId, phone.phoneNumber).map(!_))
      r ← fromDBIO(UserPhoneContactRepo.insertOrUpdate(phone))
    } yield r
  }

  protected def addEmailContact(clientUserId: Int, email: UserEmailContact) = {
    for {
      _ ← fromDBIOBoolean(ContactRpcErrors.ContactAlreadyExists)(UserEmailContactRepo.exist(email.orgId, clientUserId, email.email).map(!_))
      r ← fromDBIO(UserEmailContactRepo.insertOrUpdate(email))
    } yield r
  }

}
