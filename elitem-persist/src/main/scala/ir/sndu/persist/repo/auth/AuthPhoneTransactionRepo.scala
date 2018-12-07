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

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  override def * = (
    phoneNumber,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceInfo,
    createdAt,
    deletedAt) <> (AuthPhoneTransaction.tupled, AuthPhoneTransaction.unapply)

}

object AuthPhoneTransactionRepo {

  val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  val active = phoneTransactions.filter(_.deletedAt.isEmpty)

  val byHash = Compiled { transactionHash: Rep[String] ⇒
    active.filter(_.transactionHash === transactionHash)
  }

  val byPhoneAndDeviceHash = Compiled { (phoneNumber: Rep[Long], deviceHash: Rep[String]) ⇒
    active.filter(_.phoneNumber === phoneNumber).filter(_.deviceHash === deviceHash)
  }

  def create(transaction: AuthPhoneTransaction): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions += transaction

  def update(transactionHash: String, localDateTime: LocalDateTime): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions.filter(_.transactionHash === transactionHash).map(_.createdAt)
      .update(localDateTime)

  def find(transactionHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    byHash(transactionHash).result.headOption

  def findByPhoneAndDeviceHash(phoneNumber: Long, deviceHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    byPhoneAndDeviceHash((phoneNumber, deviceHash)).result.headOption

  def delete(transactionHash: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions.filter(_.transactionHash === transactionHash).map(_.deletedAt)
      .update(Some(LocalDateTime.now(ZoneOffset.UTC)))

}
