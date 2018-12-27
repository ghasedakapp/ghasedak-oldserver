package ir.sndu.server.model.contact

case class UserContact(
  ownerUserId:   Int,
  contactUserId: Int,
  localName:     String,
  isDeleted:     Boolean)

case class UserPhoneContact(
  phoneNumber:   Long,
  ownerUserId:   Int,
  contactUserId: Int,
  localName:     String,
  isDeleted:     Boolean)

case class UserEmailContact(
  email:         String,
  ownerUserId:   Int,
  contactUserId: Int,
  localName:     String,
  isDeleted:     Boolean)