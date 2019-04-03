package im.ghasedak.server.model.user

import im.ghasedak.api.user.ContactRecord
import im.ghasedak.api.user.ContactType.CONTACTTYPE_PHONE

@SerialVersionUID(1L)
case class UserPhone(
  id:     Int,
  userId: Int,
  orgId:  Int,
  number: Long,
  title:  String) {
  def toRecord: ContactRecord = {
    ContactRecord(
      CONTACTTYPE_PHONE,
      longValue = Some(number),
      title = Some(title))
  }
}
