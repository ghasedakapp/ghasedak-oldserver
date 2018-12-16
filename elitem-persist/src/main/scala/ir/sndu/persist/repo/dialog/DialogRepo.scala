package ir.sndu.persist.repo.dialog

import java.time.LocalDateTime

import ir.sndu.api.peer.{ ApiPeer, ApiPeerType }
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.dialog.{ Dialog, DialogCommon, UserDialog }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

final class DialogCommonTable(tag: Tag) extends Table[DialogCommon](tag, "dialog_commons") {

  def dialogId = column[String]("dialog_id", O.PrimaryKey)

  def lastMessageDate = column[LocalDateTime]("last_message_date")

  def lastMessageSeq = column[Int]("last_message_seq")

  def lastReceivedSeq = column[Int]("last_received_seq")

  def lastReadSeq = column[Int]("last_read_seq")

  def * = (dialogId, lastMessageDate, lastMessageSeq, lastReceivedSeq, lastReadSeq) <> (applyDialogCommon.tupled, unapplyDialogCommon)

  def applyDialogCommon: (String, LocalDateTime, Int, Int, Int) ⇒ DialogCommon = {
    case (
      dialogId,
      lastMessageDate,
      lastMessageSeq,
      lastReceivedSeq,
      lastReadSeq) ⇒
      DialogCommon(
        dialogId = dialogId,
        lastMessageDate = lastMessageDate,
        lastMessageSeq = lastMessageSeq,
        lastReceivedSeq = lastReceivedSeq,
        lastReadSeq = lastReadSeq)
  }

  def unapplyDialogCommon: DialogCommon ⇒ Option[(String, LocalDateTime, Int, Int, Int)] = { dc ⇒
    DialogCommon.unapply(dc).map {
      case (dialogId, lastMessageDate, lastMessageSeq, lastReceivedSeq, lastReadSeq) ⇒
        (dialogId, lastMessageDate, lastMessageSeq, lastReceivedSeq, lastReadSeq)
    }
  }
}

final class UserDialogTable(tag: Tag) extends Table[UserDialog](tag, "user_dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def ownerLastReceivedSeq = column[Int]("owner_last_received_seq")

  def ownerLastReadSeq = column[Int]("owner_last_read_seq")

  def createdAt = column[LocalDateTime]("created_at")

  def * = (
    userId,
    peerType,
    peerId,
    ownerLastReceivedSeq,
    ownerLastReadSeq,
    createdAt) <> (applyUserDialog.tupled, unapplyUserDialog)

  def applyUserDialog: (Int, Int, Int, Int, Int, LocalDateTime) ⇒ UserDialog = {
    case (
      userId,
      peerType,
      peerId,
      ownerLastReceivedSeq,
      ownerLastReadSeq,
      createdAt) ⇒
      UserDialog(
        userId = userId,
        peer = ApiPeer(ApiPeerType.fromValue(peerType), peerId),
        ownerLastReceivedSeq = ownerLastReceivedSeq,
        ownerLastReadSeq = ownerLastReadSeq,
        createdAt = createdAt)
  }

  def unapplyUserDialog: UserDialog ⇒ Option[(Int, Int, Int, Int, Int, LocalDateTime)] = { du ⇒
    UserDialog.unapply(du).map {
      case (userId, peer, ownerLastReceivedSeq, ownerLastReadSeq, createdAt) ⇒
        (userId, peer.`type`.value, peer.id, ownerLastReceivedSeq, ownerLastReadSeq, createdAt)
    }
  }
}

object DialogRepo extends UserDialogOperations with DialogCommonOperations {
  private val dialogs = for {
    c ← DialogCommonRepo.dialogCommon
    u ← UserDialogRepo.userDialogs if c.dialogId === repDialogId(u.userId, u.peerId, u.peerType)
  } yield (c, u)

  def create(
    userId:          Int,
    peer:            ApiPeer,
    lastMessageSeq:  Int,
    lastMessageDate: LocalDateTime) = {
    createUserDialog(userId, peer, 0, 0) andThen
      createCommon(DialogCommon(getDialogId(Some(userId), peer), lastMessageDate, lastMessageSeq, 0, 0))
  }

  def find(userId: Int, limit: Int)(implicit ec: ExecutionContext) = {
    dialogs.filter(r ⇒ r._2.userId === userId)
      .sortBy {
        case (common, _) ⇒ common.lastMessageDate.desc
      }.take(limit).result.map(_.map {
        case (c, u) ⇒ Dialog.from(c, u)
      })
  }
}
