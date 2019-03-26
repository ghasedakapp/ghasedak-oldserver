package im.ghasedak.server.rpc.contact

import im.ghasedak.api.contact.ContactRecord
import im.ghasedak.server.repo.user.UserAuthRepo
import im.ghasedak.server.rpc.user.UserRpcErrors

trait ContactServiceHelper {
  this: ContactServiceImpl ⇒

  def getContactRecordUserId(contactRecord: ContactRecord, orgId: Int): Result[Int] = {
    for {
      _ ← fromBoolean(ContactRpcErrors.InvalidContactRecord)(contactRecord.contact.isDefined)
      optUserId ← if (contactRecord.contact.isPhoneNumber)
        fromDBIO(UserAuthRepo.findUserIdByPhoneNumberAndOrgId(contactRecord.getPhoneNumber, orgId))
      else if (contactRecord.contact.isEmail)
        fromDBIO(UserAuthRepo.findUserIdByEmailAndOrgId(contactRecord.getEmail, orgId))
      else throw ContactRpcErrors.InvalidContactRecord
      userId ← fromOption(UserRpcErrors.UserNotFound)(optUserId)
    } yield userId
  }

}
