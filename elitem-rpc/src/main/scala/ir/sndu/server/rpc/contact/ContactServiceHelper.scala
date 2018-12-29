package ir.sndu.server.rpc.contact

import im.ghasedak.api.contact.ApiContactRecord
import ir.sndu.persist.repo.contact.{ UserContactRepo, UserEmailContactRepo, UserPhoneContactRepo }
import ir.sndu.persist.repo.user.UserAuthRepo
import ir.sndu.server.model.contact.{ UserContact, UserEmailContact, UserPhoneContact }
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
    contactRecord: ApiContactRecord): Result[Unit] = {
    for {
      _ ← if (contactRecord.contact.isPhoneNumber)
        fromDBIO(UserPhoneContactRepo.insertOrUpdate(
          UserPhoneContact(contactRecord.getPhoneNumber, ownerUserId, contactUserId, localName, isDeleted = false)))
      else
        fromDBIO(UserEmailContactRepo.insertOrUpdate(
          UserEmailContact(contactRecord.getEmail, ownerUserId, contactUserId, localName, isDeleted = false)))
    } yield ()
  }

}
