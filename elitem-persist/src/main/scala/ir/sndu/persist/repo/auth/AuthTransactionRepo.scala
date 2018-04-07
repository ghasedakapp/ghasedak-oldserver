package ir.sndu.persist.repo

import java.time.LocalDateTime

import ir.sndu.server.model.auth.AuthTransaction
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import TypeMapper._

abstract class AuthTransactionRepoBase(tag: Tag, name: String) extends Table[AuthTransaction](tag, name) {
  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)
  def appId = column[Int]("app_id")
  def apiKey = column[String]("api_key")
  def deviceHash = column[Array[Byte]]("device_hash")
  def deviceTitle = column[String]("device_title")
  def accessSalt = column[String]("access_salt")
  def deviceInfo = column[Array[Byte]]("device_info")
  def isChecked = column[Boolean]("is_checked")
  def deletedAt = column[Option[LocalDateTime]]("deleted_at")
}

class AuthTransactionRepo(tag: Tag) extends AuthTransactionRepoBase(tag, "auth_transactions") {
  def * = (
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceTitle,
    accessSalt,
    deviceInfo,
    isChecked,
    deletedAt) <> (AuthTransaction.tupled, AuthTransaction.unapply)
}

object AuthTransactionRepo {

}