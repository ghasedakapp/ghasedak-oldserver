package ir.sndu.server.model.contact

case class UserContact(
  ownerUserId:   Int,
  contactUserId: Int,
  localName:     String,
  hasPhone:      Boolean = false,
  hasEmail:      Boolean = false,
  isDeleted:     Boolean = false)