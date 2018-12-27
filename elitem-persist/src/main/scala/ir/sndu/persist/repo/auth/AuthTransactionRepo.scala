package ir.sndu.persist.repo.auth

import java.time.{ LocalDateTime, ZoneOffset }

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.auth.{ AuthTransaction, AuthTransactionBase }
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

import scala.concurrent.ExecutionContext

final class AuthTransactionTable(tag: Tag) extends AuthTransactionBaseTable[AuthTransaction](tag, "auth_transactions") {

  override def * = (
    transactionHash,
    orgId,
    apiKey,
    createdAt,
    isChecked,
    deletedAt) <> (AuthTransaction.tupled, AuthTransaction.unapply)

}

object AuthTransactionRepo {

  val transactions = TableQuery[AuthTransactionTable]

  private val active = transactions.filter(_.deletedAt.isEmpty)

  def find(transactionHash: String): SqlAction[Option[AuthTransaction], NoStream, Effect.Read] =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def findChildren(transactionHash: String)(implicit ec: ExecutionContext): DBIO[Option[AuthTransactionBase]] =
    for {
      phone ← AuthPhoneTransactionRepo.find(transactionHash)
    } yield phone match {
      case Some(_) ⇒ phone
      case _       ⇒ None
    }

  def delete(transactionHash: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    transactions.filter(_.transactionHash === transactionHash)
      .map(_.deletedAt).update(Some(LocalDateTime.now(ZoneOffset.UTC)))

  def updateCreateAt(transactionHash: String, createAt: LocalDateTime): FixedSqlAction[Int, NoStream, Effect.Write] =
    transactions.filter(_.transactionHash === transactionHash).map(_.createdAt).update(createAt)

  def updateSetChecked(transactionHash: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    transactions.filter(_.transactionHash === transactionHash).map(_.isChecked).update(true)

}
