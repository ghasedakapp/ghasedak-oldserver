package im.ghasedak.server.model.auth

import java.time.LocalDateTime

final case class AuthTransaction(
  transactionHash: String,
  orgId:           Int,
  apiKey:          String,
  createdAt:       LocalDateTime,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None)

final case class AuthPhoneTransaction(
  phoneNumber:     Long,
  transactionHash: String,
  orgId:           Int,
  apiKey:          String,
  createdAt:       LocalDateTime,
  isChecked:       Boolean               = false,
  deletedAt:       Option[LocalDateTime] = None) extends AuthTransactionBase

sealed trait AuthTransactionBase {

  def transactionHash: String

  def orgId: Int

  def apiKey: String

  def createdAt: LocalDateTime

  def isChecked: Boolean

  def deletedAt: Option[LocalDateTime]

}