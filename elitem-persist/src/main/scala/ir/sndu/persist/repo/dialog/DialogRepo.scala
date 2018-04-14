package ir.sndu.persist.repo.dialog

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.dialog.{ Dialog, DialogCommon, UserDialog }
import ir.sndu.server.peer.{ ApiPeer, ApiPeerType }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import ir.sndu.server.utils._

import scala.concurrent.ExecutionContext

final class DialogCommonTable(tag: Tag) extends Table[DialogCommon](tag, "dialog_commons") {

  def dialogId = column[String]("dialog_id", O.PrimaryKey)

  def lastMessageDate = column[LocalDateTime]("last_message_date")

  def lastReceivedAt = column[LocalDateTime]("last_received_at")

  def lastReadAt = column[LocalDateTime]("last_read_at")

  def * = (dialogId, lastMessageDate, lastReceivedAt, lastReadAt) <> (applyDialogCommon.tupled, unapplyDialogCommon)

  def applyDialogCommon: (String, LocalDateTime, LocalDateTime, LocalDateTime) ⇒ DialogCommon = {
    case (
      dialogId,
      lastMessageDate,
      lastReceivedAt,
      lastReadAt) ⇒
      DialogCommon(
        dialogId = dialogId,
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt)
  }

  def unapplyDialogCommon: DialogCommon ⇒ Option[(String, LocalDateTime, LocalDateTime, LocalDateTime)] = { dc ⇒
    DialogCommon.unapply(dc).map {
      case (dialogId, lastMessageDate, lastReceivedAt, lastReadAt) ⇒
        (dialogId, lastMessageDate, lastReceivedAt, lastReadAt)
    }
  }
}

final class UserDialogTable(tag: Tag) extends Table[UserDialog](tag, "user_dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def ownerLastReceivedAt = column[LocalDateTime]("owner_last_received_at")

  def ownerLastReadAt = column[LocalDateTime]("owner_last_read_at")

  def createdAt = column[LocalDateTime]("created_at")

  def isFavourite = column[Boolean]("is_favourite")

  def * = (
    userId,
    peerType,
    peerId,
    ownerLastReceivedAt,
    ownerLastReadAt,
    createdAt,
    isFavourite) <> (applyUserDialog.tupled, unapplyUserDialog)

  def applyUserDialog: (Int, Int, Int, LocalDateTime, LocalDateTime, LocalDateTime, Boolean) ⇒ UserDialog = {
    case (
      userId,
      peerType,
      peerId,
      ownerLastReceivedAt,
      ownerLastReadAt,
      createdAt,
      isFavourite) ⇒
      UserDialog(
        userId = userId,
        peer = ApiPeer(ApiPeerType.fromValue(peerType), peerId),
        ownerLastReceivedAt = ownerLastReceivedAt,
        ownerLastReadAt = ownerLastReadAt,
        createdAt = createdAt,
        isFavourite = isFavourite)
  }

  def unapplyUserDialog: UserDialog ⇒ Option[(Int, Int, Int, LocalDateTime, LocalDateTime, LocalDateTime, Boolean)] = { du ⇒
    UserDialog.unapply(du).map {
      case (userId, peer, ownerLastReceivedAt, ownerLastReadAt, createdAt, isFavourite) ⇒
        (userId, peer.`type`.value, peer.id, ownerLastReceivedAt, ownerLastReadAt, createdAt, isFavourite)
    }
  }
}

object DialogRepo extends UserDialogOperations with DialogCommonOperations {
  import ImplicitTimes._
  private val dialogs = for {
    c ← DialogCommonRepo.dialogCommon
    u ← UserDialogRepo.userDialogs if c.dialogId === repDialogId(u.userId, u.peerId, u.peerType)
  } yield (c, u)

  def create(
    userId: Int,
    peer: ApiPeer,
    lastMessageDate: LocalDateTime) = {
    createUserDialog(userId, peer, DateTimeHelper.origin, DateTimeHelper.origin) andThen
      createCommon(DialogCommon(getDialogId(Some(userId), peer), lastMessageDate, DateTimeHelper.origin, DateTimeHelper.origin))
  }

  def find(userId: Int, limit: Int)(implicit ec: ExecutionContext) = {
    dialogs.filter(r => r._2.userId === userId)
      .sortBy {
        case (common, _) => common.lastMessageDate.desc
      }.take(limit).result.map(_.map {
        case (c, u) => Dialog.from(c, u)
      })
  }
}
