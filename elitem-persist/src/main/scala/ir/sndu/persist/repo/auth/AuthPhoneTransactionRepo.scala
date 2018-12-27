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
    orgId,
    apiKey,
    createdAt,
    isChecked,
    deletedAt) <> (AuthPhoneTransaction.tupled, AuthPhoneTransaction.unapply)

}

object AuthPhoneTransactionRepo {

  val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  val active = phoneTransactions.filter(_.deletedAt.isEmpty)

  def create(transaction: AuthPhoneTransaction): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneTransactions += transaction

  def find(transactionHash: String): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def findByPhoneNumberAndOrgId(phoneNumber: Long, orgId: Int): SqlAction[Option[AuthPhoneTransaction], NoStream, Effect.Read] =
    active.filter(_.phoneNumber === phoneNumber).filter(_.orgId === orgId).filter(_.isChecked === false).result.headOption

}
