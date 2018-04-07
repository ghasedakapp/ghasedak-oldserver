package ir.sndu.persist.repo.dialog

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.Peer
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

  def findCommon(userId: Option[Int], peer: Peer): DBIO[Option[DialogCommon]] =
    byPKC.applied(getDialogId(userId, peer)).result.headOption

  def commonExists(dialogId: String) = existsC(dialogId).result

  def updateLastMessageDatePrivate(userId: Int, peer: Peer, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastMessageDate).update(lastMessageDate)
  }

  def updateLastMessageDateGroup(peer: Peer, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer))
      .map(_.lastMessageDate)
      .update(lastMessageDate)
  }

  def updateLastReceivedAtPrivate(userId: Int, peer: Peer, lastReceivedAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReceivedAt).update(lastReceivedAt)
  }

  def updateLastReceivedAtGroup(peer: Peer, lastReceivedAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReceivedAt).update(lastReceivedAt)
  }

  def updateLastReadAtPrivate(userId: Int, peer: Peer, lastReadAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReadAt).update(lastReadAt)
  }

  def updateLastReadAtGroup(peer: Peer, lastReadAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReadAt).update(lastReadAt)
  }

  def requirePrivate(peer: Peer) = require(peer.`type`.isPrivate, "It should be private peer")

  def requireGroup(peer: Peer) = require(peer.`type`.isGroup, "It should be group peer")
}
