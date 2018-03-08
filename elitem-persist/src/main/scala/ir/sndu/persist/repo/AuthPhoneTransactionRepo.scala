package ir.sndu.persist.repo

import java.time.LocalDateTime

import ir.sndu.server.model.auth.{AuthPhoneTransaction, AuthTransaction}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import TypeMapper._

class AuthPhoneTransactionRepo(tag: Tag) extends AuthTransactionRepoBase(tag, "auth_phone_transactions") with API {
  def phoneNumber = column[Long]("phone_number")

  def * = (
    phoneNumber,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceTitle,
    accessSalt,
    deviceInfo,
    isChecked,
    deletedAt) <> (AuthPhoneTransaction.tupled, AuthPhoneTransaction.unapply)
}


