package ir.sndu.server.model.auth

import java.time.LocalDateTime

@SerialVersionUID(1L)
final case class AuthTransaction(
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      String,
  deviceInfo:      String,
  createdAt:       LocalDateTime,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None)

@SerialVersionUID(1L)
final case class AuthPhoneTransaction(
  phoneNumber:     Long,
  transactionHash: String,
  appId:           Int,
  apiKey:          String,
  deviceHash:      String,
  deviceInfo:      String,
  createdAt:       LocalDateTime,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None) extends AuthTransactionBase

sealed trait AuthTransactionBase {

  def transactionHash: String

  def appId: Int

  def apiKey: String

  def deviceHash: String

  def deviceInfo: String

  def createdAt: LocalDateTime

  def isChecked: Boolean

}