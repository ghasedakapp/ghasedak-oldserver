package ir.sndu.persist.repo.auth

import java.time.{ LocalDateTime, ZoneOffset }

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthPhoneTransaction
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

class AuthPhoneTransactionTable(tag: Tag) extends Table[AuthPhoneTransaction](tag, "auth_phone_transactions") {

  def phoneNumber = column[Long]("phone_number")

  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)

  def appId = column[Int]("app_id")

  def apiKey = column[String]("api_key")

  def deviceHash = column[String]("device_hash")

  def deviceInfo = column[String]("device_info")

  def createdAt = column[LocalDateTime]("created_at")

  def codeHash = column[String]("code_hash")

  def isChecked = column[Boolean]("is_checked")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (
    phoneNumber,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceInfo,
    createdAt,
    codeHash,
    isChecked,
    deletedAt) <> (AuthPhoneTransaction.tupled, AuthPhoneTransaction.unapply)

}

object AuthPhoneTransactionRepo {

  private val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  private val active = phoneTransactions.filter(_.deletedAt.isEmpty).filter(_.isChecked === true)

  private val byHash = Compiled { transactionHash: Rep[String] ⇒
    active.filter(_.transactionHash === transactionHash)
  }

  private val byPhoneAndDeviceHash = Compiled { (phoneNumber: Rep[Long], deviceHash: Rep[String]) ⇒
    active.filter(_.phoneNumber === phoneNumber).filter(_.deviceHash === deviceHash)
  }

  def create(transaction: AuthPhoneTransaction): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions += transaction

  def updateCreateAt(transactionHash: String, localDateTime: LocalDateTime): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions.filter(_.transactionHash === transactionHash).map(_.createdAt)
      .update(localDateTime)

  def findByHash(transactionHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    byHash(transactionHash).result.headOption

  def findByPhoneAndDeviceHash(phoneNumber: Long, deviceHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    byPhoneAndDeviceHash((phoneNumber, deviceHash)).result.headOption

  def delete(transactionHash: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions.filter(_.transactionHash === transactionHash).map(_.deletedAt)
      .update(Some(LocalDateTime.now(ZoneOffset.UTC)))

  def updateIsChecked(transactionHash: String, isChecked: Boolean): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions.filter(_.transactionHash === transactionHash).map(_.isChecked).update(isChecked)

}
