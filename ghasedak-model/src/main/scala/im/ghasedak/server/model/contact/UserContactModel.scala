package im.ghasedak.server.model.contact

import java.time.LocalDateTime

case class UserContactModel(
  ownerUserId:   Int,
  contactUserId: Int,
  orgId:         Int,
  localName:     Option[String],
  deletedAt:     Option[LocalDateTime])

case class UserPhoneContact(
  phoneNumber:   Long,
  ownerUserId:   Int,
  contactUserId: Int,
  orgId:         Int,
  localName:     Option[String],
  deletedAt:     Option[LocalDateTime])

case class UserEmailContact(
  email:         String,
  ownerUserId:   Int,
  contactUserId: Int,
  orgId:         Int,
  localName:     Option[String],
  deletedAt:     Option[LocalDateTime])