package ir.sndu.server.model.auth

import java.time.LocalDateTime

/**
 * Parent model that contains authorization info before user log in.
 */
final case class AuthTransaction(
  transactionHash: String,
  appId: Int,
  apiKey: String,
  deviceHash: Array[Byte],
  deviceTitle: String,
  accessSalt: String,
  deviceInfo: Array[Byte],
  isChecked: Boolean = false,
  deletedAt: Option[LocalDateTime] = None)

final case class AuthPhoneTransaction(
  phoneNumber: Long,
  transactionHash: String,
  appId: Int,
  apiKey: String,
  deviceHash: Array[Byte],
  deviceTitle: String,
  accessSalt: String,
  deviceInfo: Array[Byte],
  isChecked: Boolean = false,
  deletedAt: Option[LocalDateTime] = None) extends AuthTransactionBase

sealed trait AuthTransactionBase {
  def transactionHash: String
  def appId: Int
  def apiKey: String
  def deviceHash: Array[Byte]
  def deviceTitle: String
  def isChecked: Boolean
  def accessSalt: String
  def deviceInfo: Array[Byte]
}