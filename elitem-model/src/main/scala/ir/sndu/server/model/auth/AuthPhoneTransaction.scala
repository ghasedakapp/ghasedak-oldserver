package ir.sndu.server.model.auth

import java.time.LocalDateTime

@SerialVersionUID(1L)
case class AuthPhoneTransaction(
  phoneNumber:     Long,
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      String,
  deviceInfo:      String,
  deletedAt:       Option[LocalDateTime] = None)