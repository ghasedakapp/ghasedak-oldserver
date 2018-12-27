package ir.sndu.server.model.user

final case class UserInfo(
  userId:      Int,
  countryCode: Option[String] = None,
  nickname:    Option[String] = None,
  about:       Option[String] = None)