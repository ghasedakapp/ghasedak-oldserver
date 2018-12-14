package ir.sndu.persist.repo.auth

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.AuthPhoneTransaction
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class AuthPhoneTransactionTable(tag: Tag) extends AuthTransactionBaseTable[AuthPhoneTransaction](tag, "auth_phone_transactions") with InheritingTable {

  def phoneNumber = column[Long]("phone_number")

  override val inherited: AuthTransactionTable = AuthTransactionRepo.transactions.baseTableRow

  override def * = (
    phoneNumber,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceInfo,
    createdAt,
    isChecked,
    deletedAt) <> (AuthPhoneTransaction.tupled, AuthPhoneTransaction.unapply)

}

object AuthPhoneTransactionRepo {

  private val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  private val active = phoneTransactions.filter(_.deletedAt.isEmpty)

  private val byHash = Compiled { hash: Rep[String] ⇒
    active.filter(_.transactionHash === hash)
  }

  private val byPhoneAndDeviceHash = Compiled { (phone: Rep[Long], deviceHash: Rep[String]) ⇒
    active.filter(t ⇒ t.phoneNumber === phone && t.deviceHash === deviceHash)
  }

  def create(transaction: AuthPhoneTransaction): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions += transaction

  def find(transactionHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    byHash(transactionHash).result.headOption

  def findByPhoneAndDeviceHash(phone: Long, deviceHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    byPhoneAndDeviceHash((phone, deviceHash)).result.headOption

}
