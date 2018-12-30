package ir.sndu.server.rpc.contact

import im.ghasedak.api.contact.ApiContactRecord
import ir.sndu.persist.repo.contact.UserContactRepo
import ir.sndu.persist.repo.user.UserAuthRepo
import ir.sndu.server.model.contact.UserContact
import ir.sndu.server.rpc.user.UserRpcErrors

trait ContactServiceHelper {
  this: ContactServiceImpl ⇒

  def getContactRecordUserId(contactRecord: ApiContactRecord, orgId: Int): Result[Int] = {
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

  def addUserContact(
    ownerUserId:   Int,
    contactUserId: Int,
    localName:     String,
    contactRecord: ApiContactRecord): Result[Int] =
    if (contactRecord.contact.isPhoneNumber)
      fromDBIO(UserContactRepo.insertOrUpdate(
        UserContact(ownerUserId, contactUserId, localName, hasPhone = true)))
    else
      fromDBIO(UserContactRepo.insertOrUpdate(
        UserContact(ownerUserId, contactUserId, localName, hasEmail = true)))

}
