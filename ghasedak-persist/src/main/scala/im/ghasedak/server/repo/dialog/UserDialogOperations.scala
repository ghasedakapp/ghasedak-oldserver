package im.ghasedak.server.repo.dialog

import java.time.{ LocalDateTime, ZoneId }

import im.ghasedak.server.model.dialog.UserDialog
import slick.dbio.Effect
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.ExecutionContext

object UserDialogRepo {

  val userDialogs = TableQuery[UserDialogTable]

  val byPKC = Compiled(byPK _)

  val notArchived = userDialogs

  private def byPK(chatId: Rep[Long]) =
    userDialogs.filter(u ⇒ u.chatId === chatId)

}

trait UserDialogOperations {
  import UserDialogRepo._

  def createUserDialog(
    userId:               Int,
    chatId:               Long,
    ownerLastReceivedSeq: Int,
    ownerLastReadSeq:     Int): FixedSqlAction[Int, NoStream, Effect.Write] =
    userDialogs insertOrUpdate UserDialog(
      userId,
      chatId,
      ownerLastReceivedSeq,
      ownerLastReadSeq,
      LocalDateTime.now(ZoneId.systemDefault()))

  def findUsersVisible(userId: Rep[Int]) = notArchived.filter(_.userId === userId)

  def findUsers(chatId: Long): DBIO[Option[UserDialog]] =
    byPKC.applied(chatId).result.headOption

  def usersExists(chatId: Long): FixedSqlAction[Boolean, PostgresProfile.api.NoStream, Effect.Read] =
    byPKC.applied(chatId).exists.result

  def updateOwnerLastReceivedSeq(chatId: Long, ownerLastReceivedSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied(chatId).map(_.ownerLastReceivedSeq).update(ownerLastReceivedSeq)

  def updateOwnerLastReadSeq(chatId: Long, ownerLastReadSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied(chatId).map(_.ownerLastReadSeq).update(ownerLastReadSeq)

  def delete(chatId: Long): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied(chatId).delete

}
