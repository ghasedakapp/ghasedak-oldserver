package ir.sndu.persist.repo.dialog

import java.time.LocalDateTime

import ir.sndu.api.peer.{ ApiPeer, ApiPeerType }
import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.dialog.DialogCommon
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object DialogCommonRepo {
  val dialogCommon = TableQuery[DialogCommonTable]

  private def byPK(dialogId: Rep[String]) =
    dialogCommon.filter(_.dialogId === dialogId)

  private def exists(dialogId: Rep[String]) = byPK(dialogId).exists

  val byPKC = Compiled(byPK _)
  val existsC = Compiled(exists _)
}

trait DialogCommonOperations extends DialogId {
  import DialogCommonRepo._

  def createCommon(common: DialogCommon) =
    dialogCommon insertOrUpdate common

  def findCommon(userId: Option[Int], peer: ApiPeer): DBIO[Option[DialogCommon]] =
    byPKC.applied(getDialogId(userId, peer)).result.headOption

  def commonExists(dialogId: String) = existsC(dialogId).result

  def updateLastMessageDate(userId: Int, peer: ApiPeer, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext) =
    peer.`type` match {
      case ApiPeerType.PEER_TYPE_UNKNOWN | ApiPeerType.Unrecognized(_) ⇒ throw new RuntimeException("Unknown peer type")
      case ApiPeerType.PRIVATE ⇒ updateLastMessageDatePrivate(userId: Int, peer: ApiPeer, lastMessageDate: LocalDateTime)
      case ApiPeerType.GROUP ⇒ updateLastMessageDateGroup(peer, lastMessageDate)

    }

  def updateLastMessageDatePrivate(userId: Int, peer: ApiPeer, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastMessageDate).update(lastMessageDate)
  }

  def updateLastMessageDateGroup(peer: ApiPeer, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer))
      .map(_.lastMessageDate)
      .update(lastMessageDate)
  }

  def updateLastReceivedAtPrivate(userId: Int, peer: ApiPeer, lastReceivedSeq: Int)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReceivedSeq).update(lastReceivedSeq)
  }

  def updateLastReceivedSeqGroup(peer: ApiPeer, lastReceivedSeq: Int)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReceivedSeq).update(lastReceivedSeq)
  }

  def updateLastReadSeqPrivate(userId: Int, peer: ApiPeer, lastReadSeq: Int)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReadSeq).update(lastReadSeq)
  }

  def updateLastReadSeqGroup(peer: ApiPeer, lastReadSeq: Int)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReadSeq).update(lastReadSeq)
  }

  def requirePrivate(peer: ApiPeer) = require(peer.`type`.isPrivate, "It should be private peer")

  def requireGroup(peer: ApiPeer) = require(peer.`type`.isGroup, "It should be group peer")
}
