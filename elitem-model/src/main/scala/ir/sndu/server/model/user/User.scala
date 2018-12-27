package ir.sndu.server.model.user

import java.time.LocalDateTime

import im.ghasedak.api.user.ApiUser

@SerialVersionUID(1L)
final case class User(
  id:          Int,
  name:        String,
  countryCode: String,
  createdAt:   LocalDateTime,
  nickname:    Option[String]        = None,
  about:       Option[String]        = None,
  deletedAt:   Option[LocalDateTime] = None) {
  def toApi: ApiUser = {
    ApiUser(
      id = id,
      name = name,
      nickname = nickname,
      about = about)
  }
}