package ir.sndu.server.model.user

import im.ghasedak.api.contact.ApiContactRecord

final case class UserAuth(
  orgId:       Int,
  userId:      Int,
  phoneNumber: Option[Long]   = None,
  email:       Option[String] = None,
  nickname:    Option[String] = None,
  countryCode: Option[String] = None,
  isDeleted:   Boolean        = false) {
  def toApiContact: Seq[ApiContactRecord] = {
    Seq(
      phoneNumber.map(ApiContactRecord().withPhoneNumber),
      email.map(ApiContactRecord().withEmail),
      nickname.map(ApiContactRecord().withNickname)).flatten
  }
}