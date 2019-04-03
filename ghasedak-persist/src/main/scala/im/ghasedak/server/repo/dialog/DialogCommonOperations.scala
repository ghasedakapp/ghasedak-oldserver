package im.ghasedak.server.repo.dialog

import java.time.LocalDateTime

import im.ghasedak.server.model.dialog.DialogCommon
import im.ghasedak.server.repo.TypeMapper._
import slick.dbio.Effect
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.ExecutionContext

object DialogCommonRepo {

  val dialogCommon = TableQuery[DialogCommonTable]

  private def byPK(chatId: Rep[Long]) =
    dialogCommon.filter(_.chatId === chatId)

  private def exists(chatId: Rep[Long]) = byPK(chatId).exists

  val byPKC = Compiled(byPK _)
  val existsC = Compiled(exists _)

}

trait DialogCommonOperations {

  import DialogCommonRepo._

  def createCommon(common: DialogCommon): FixedSqlAction[Int, NoStream, Effect.Write] =
    dialogCommon insertOrUpdate common

  def findCommon(chatId: Long): DBIO[Option[DialogCommon]] =
    byPKC.applied(chatId).result.headOption

  def commonExists(chatId: Long): FixedSqlAction[Boolean, PostgresProfile.api.NoStream, Effect.Read] = existsC(chatId).result

  def updateLastMessageSeqDate(chatId: Long, lastMessageSeq: Int, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied(chatId).map(r ⇒ (r.lastMessageSeq, r.lastMessageDate)).update((lastMessageSeq, lastMessageDate))

  def updateLastReceivedSeq(chatId: Long, lastReceivedSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied(chatId).map(_.lastReceivedSeq).update(lastReceivedSeq)

  def updateLastReadSeq(chatId: Long, lastReadSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied(chatId).map(_.lastReadSeq).update(lastReadSeq)

}
