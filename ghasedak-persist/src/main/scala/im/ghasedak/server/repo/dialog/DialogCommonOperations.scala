package im.ghasedak.server.repo.dialog

import java.time.LocalDateTime

import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import im.ghasedak.server.repo.TypeMapper._
import im.ghasedak.server.model.dialog.DialogCommon
import slick.dbio.Effect
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

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

  def createCommon(common: DialogCommon): FixedSqlAction[Int, NoStream, Effect.Write] =
    dialogCommon insertOrUpdate common

  def findCommon(userId: Option[Int], peer: ApiPeer): DBIO[Option[DialogCommon]] =
    byPKC.applied(getDialogId(userId, peer)).result.headOption

  def commonExists(dialogId: String): FixedSqlAction[Boolean, PostgresProfile.api.NoStream, Effect.Read] = existsC(dialogId).result

  def updateLastMessageSeqDate(userId: Int, peer: ApiPeer, lastMessageSeq: Int, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    peer.`type` match {
      case ApiPeerType.UNKNOWN | ApiPeerType.Unrecognized(_) ⇒ throw new RuntimeException("Unknown peer type")
      case ApiPeerType.PRIVATE                               ⇒ updateLastMessageSeqDatePrivate(userId: Int, peer: ApiPeer, lastMessageSeq, lastMessageDate: LocalDateTime)
      case ApiPeerType.GROUP                                 ⇒ updateLastMessageSeqDateGroup(peer, lastMessageSeq, lastMessageDate)
    }

  def updateLastReceivedSeq(userId: Int, peer: ApiPeer, lastReceivedSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    peer.`type` match {
      case ApiPeerType.UNKNOWN | ApiPeerType.Unrecognized(_) ⇒ throw new RuntimeException("Unknown peer type")
      case ApiPeerType.PRIVATE                               ⇒ updateLastReceivedSeqPrivate(userId: Int, peer: ApiPeer, lastReceivedSeq)
      case ApiPeerType.GROUP                                 ⇒ updateLastReceivedSeqGroup(peer, lastReceivedSeq)
    }

  def updateLastReadSeq(userId: Int, peer: ApiPeer, lastReadSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    peer.`type` match {
      case ApiPeerType.UNKNOWN | ApiPeerType.Unrecognized(_) ⇒ throw new RuntimeException("Unknown peer type")
      case ApiPeerType.PRIVATE                               ⇒ updateLastReadSeqPrivate(userId: Int, peer: ApiPeer, lastReadSeq)
      case ApiPeerType.GROUP                                 ⇒ updateLastReadSeqGroup(peer, lastReadSeq)
    }

  def updateLastMessageSeqDatePrivate(userId: Int, peer: ApiPeer, lastMessageSeq: Int, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(r ⇒ (r.lastMessageSeq, r.lastMessageDate)).update((lastMessageSeq, lastMessageDate))
  }

  def updateLastMessageSeqDateGroup(peer: ApiPeer, lastMessageSeq: Int, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer))
      .map(r ⇒ (r.lastMessageSeq, r.lastMessageDate))
      .update((lastMessageSeq, lastMessageDate))
  }

  def updateLastReceivedSeqPrivate(userId: Int, peer: ApiPeer, lastReceivedSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReceivedSeq).update(lastReceivedSeq)
  }

  def updateLastReceivedSeqGroup(peer: ApiPeer, lastReceivedSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReceivedSeq).update(lastReceivedSeq)
  }

  def updateLastReadSeqPrivate(userId: Int, peer: ApiPeer, lastReadSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] = {
    requirePrivate(peer)
    byPKC.applied(getDialogId(Some(userId), peer)).map(_.lastReadSeq).update(lastReadSeq)
  }

  def updateLastReadSeqGroup(peer: ApiPeer, lastReadSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] = {
    requireGroup(peer)
    byPKC.applied(getDialogId(None, peer)).map(_.lastReadSeq).update(lastReadSeq)
  }

  def requirePrivate(peer: ApiPeer): Unit = require(peer.`type`.isPrivate, "It should be private peer")

  def requireGroup(peer: ApiPeer): Unit = require(peer.`type`.isGroup, "It should be group peer")

}
