package ir.sndu.server.model.user

import java.time.LocalDateTime

@SerialVersionUID(1L)
case class User(
  id:          Int,
  accessSalt:  String,
  name:        String,
  countryCode: String,
  createdAt:   LocalDateTime,
  nickname:    Option[String]        = None,
  about:       Option[String]        = None,
  deletedAt:   Option[LocalDateTime] = None)