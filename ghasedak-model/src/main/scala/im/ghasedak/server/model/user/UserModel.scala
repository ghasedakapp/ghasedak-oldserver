package im.ghasedak.server.model.user

import java.time.LocalDateTime

import im.ghasedak.api.user.{ Lifecycle, Sex, User, UserData }
import im.ghasedak.server.model.TimeConversions._

final case class UserModel(
  id:          Int,
  orgId:       Int,
  name:        String,
  sex:         Sex                   = Sex.SEX_UNKNOWN,
  createdAt:   LocalDateTime,
  isBot:       Boolean               = false,
  about:       Option[String]        = None,
  deletedAt:   Option[LocalDateTime] = None,
  nickname:    Option[String]        = None,
  countryCode: Option[String]        = None) {

  def toApi(localName: Option[String] = None): User = {
    val c = deletedAt.getOrElse(createdAt)
    User(
      id,
      Some(UserData(
        name = localName.getOrElse(name),
        nick = nickname,
        sex = sex,
        isBot = Some(isBot),
        status = deletedAt.map(_ ⇒ Lifecycle.DELETED).getOrElse(Lifecycle.ACTIVE),
        clock = Some(c))))
  }

}
