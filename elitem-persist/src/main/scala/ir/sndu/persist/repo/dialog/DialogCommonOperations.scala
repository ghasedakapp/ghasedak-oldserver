package ir.sndu.persist.repo.dialog

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.dialog.DialogCommon
import ir.sndu.server.peer.{ ApiPeer, ApiPeerType }
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
      case ApiPeerType.Private => updateLastMessageDatePrivate(userId: Int, peer: ApiPeer, lastMessageDate: LocalDateTime)
      case ApiPeerType.Group => updateLastMessageDateGroup(peer, lastMessageDate)
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

  def updateLastReceivedAtPrivate(userId: Int, peer: ApiPeer, lastReceivedAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReceivedAt).update(lastReceivedAt)
  }

  def updateLastReceivedAtGroup(peer: ApiPeer, lastReceivedAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReceivedAt).update(lastReceivedAt)
  }

  def updateLastReadAtPrivate(userId: Int, peer: ApiPeer, lastReadAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReadAt).update(lastReadAt)
  }

  def updateLastReadAtGroup(peer: ApiPeer, lastReadAt: LocalDateTime)(implicit ec: ExecutionContext) = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReadAt).update(lastReadAt)
  }

  def requirePrivate(peer: ApiPeer) = require(peer.`type`.isPrivate, "It should be private peer")

  def requireGroup(peer: ApiPeer) = require(peer.`type`.isGroup, "It should be group peer")
}
