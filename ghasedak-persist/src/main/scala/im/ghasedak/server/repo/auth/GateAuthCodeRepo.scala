package im.ghasedak.server.repo.auth

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.auth.GateAuthCode
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, SqlAction }

final class GateAuthCodeTable(tag: Tag) extends Table[GateAuthCode](tag, "gate_auth_codes") {

  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)

  def codeHash = column[String]("code_hash")

  def isDeleted = column[Boolean]("is_deleted")

  def * = (transactionHash, codeHash, isDeleted) <> (GateAuthCode.tupled, GateAuthCode.unapply)

}

object GateAuthCodeRepo {

  val codes = TableQuery[GateAuthCodeTable]

  val active = codes.filter(_.isDeleted === false)

  def createOrUpdate(transactionHash: String, codeHash: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    codes.insertOrUpdate(GateAuthCode(transactionHash, codeHash))

  def find(transactionHash: String): SqlAction[Option[GateAuthCode], NoStream, Effect.Read] =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def delete(transactionHash: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    codes.filter(_.transactionHash === transactionHash).map(_.isDeleted).update(true)

}
