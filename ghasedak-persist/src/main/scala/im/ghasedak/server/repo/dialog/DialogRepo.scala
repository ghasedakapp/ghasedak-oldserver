package im.ghasedak.server.repo.dialog

import java.time.LocalDateTime

import im.ghasedak.server.model.dialog.{ Dialog, DialogCommon, UserDialog }
import im.ghasedak.server.repo.TypeMapper._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

final class DialogCommonTable(tag: Tag) extends Table[DialogCommon](tag, "dialog_commons") {

  def chatId = column[Long]("chat_id", O.PrimaryKey)

  def lastMessageDate = column[LocalDateTime]("last_message_date")

  def lastMessageSeq = column[Int]("last_message_seq")

  def lastReceivedSeq = column[Int]("last_received_seq")

  def lastReadSeq = column[Int]("last_read_seq")

  def * = (chatId, lastMessageDate, lastMessageSeq, lastReceivedSeq, lastReadSeq) <> (applyDialogCommon.tupled, unapplyDialogCommon)

  def applyDialogCommon: (Long, LocalDateTime, Int, Int, Int) ⇒ DialogCommon = {
    case (
      chatId,
      lastMessageDate,
      lastMessageSeq,
      lastReceivedSeq,
      lastReadSeq) ⇒
      DialogCommon(
        chatId = chatId,
        lastMessageDate = lastMessageDate,
        lastMessageSeq = lastMessageSeq,
        lastReceivedSeq = lastReceivedSeq,
        lastReadSeq = lastReadSeq)
  }

  def unapplyDialogCommon: DialogCommon ⇒ Option[(Long, LocalDateTime, Int, Int, Int)] = { dc ⇒
    DialogCommon.unapply(dc).map {
      case (chatId, lastMessageDate, lastMessageSeq, lastReceivedSeq, lastReadSeq) ⇒
        (chatId, lastMessageDate, lastMessageSeq, lastReceivedSeq, lastReadSeq)
    }
  }
}

final class UserDialogTable(tag: Tag) extends Table[UserDialog](tag, "user_dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def chatId = column[Long]("chat_id", O.PrimaryKey)

  def ownerLastReceivedSeq = column[Int]("owner_last_received_seq")

  def ownerLastReadSeq = column[Int]("owner_last_read_seq")

  def createdAt = column[LocalDateTime]("created_at")

  def * = (
    userId,
    chatId,
    ownerLastReceivedSeq,
    ownerLastReadSeq,
    createdAt) <> (applyUserDialog.tupled, unapplyUserDialog)

  def applyUserDialog: (Int, Long, Int, Int, LocalDateTime) ⇒ UserDialog = {
    case (
      userId,
      chatId,
      ownerLastReceivedSeq,
      ownerLastReadSeq,
      createdAt) ⇒
      UserDialog(
        userId = userId,
        chatId = chatId,
        ownerLastReceivedSeq = ownerLastReceivedSeq,
        ownerLastReadSeq = ownerLastReadSeq,
        createdAt = createdAt)
  }

  def unapplyUserDialog: UserDialog ⇒ Option[(Int, Long, Int, Int, LocalDateTime)] = { du ⇒
    UserDialog.unapply(du).map {
      case (userId, chatId, ownerLastReceivedSeq, ownerLastReadSeq, createdAt) ⇒
        (userId, chatId, ownerLastReceivedSeq, ownerLastReadSeq, createdAt)
    }
  }
}

object DialogRepo extends UserDialogOperations with DialogCommonOperations {

  val dialogs = for {
    c ← DialogCommonRepo.dialogCommon
    u ← UserDialogRepo.userDialogs if c.chatId === u.chatId
  } yield (c, u)

  def create(
    userId:          Int,
    chatId:          Long,
    lastMessageSeq:  Int,
    lastMessageDate: LocalDateTime): DBIOAction[Int, PostgresProfile.api.NoStream, Effect.Write with Effect.Write] = {
    createUserDialog(userId, chatId, 0, 0) andThen
      createCommon(DialogCommon(chatId, lastMessageDate, lastMessageSeq, 0, 0))
  }

  def find(userId: Int, limit: Int)(implicit ec: ExecutionContext): DBIOAction[Seq[Dialog], NoStream, Effect.Read] = {
    dialogs.filter(r ⇒ r._2.userId === userId)
      .sortBy {
        case (common, _) ⇒ common.lastMessageDate.desc
      }.take(limit).result.map(_.map {
        case (c, u) ⇒ Dialog.from(c, u)
      })
  }

}
