package im.ghasedak.server.model.user

final case class UserAuth(
  orgId:       Int,
  userId:      Int,
  phoneNumber: Option[Long]   = None,
  email:       Option[String] = None,
  nickname:    Option[String] = None,
  countryCode: Option[String] = None,
  isDeleted:   Boolean        = false)