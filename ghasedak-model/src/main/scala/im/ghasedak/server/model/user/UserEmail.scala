package im.ghasedak.server.model.user

import im.ghasedak.api.user.ContactRecord
import im.ghasedak.api.user.ContactType.CONTACTTYPE_EMAIL

@SerialVersionUID(1L)
case class UserEmail(
  id:     Int,
  userId: Int,
  orgId:  Int,
  email:  String,
  title:  String) {
  def toRecord: ContactRecord = {
    ContactRecord(
      CONTACTTYPE_EMAIL,
      stringValue = Some(email),
      title = Some(title))
  }
}
