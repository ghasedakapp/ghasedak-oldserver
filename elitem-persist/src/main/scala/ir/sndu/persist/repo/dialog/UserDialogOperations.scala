package ir.sndu.persist.repo.dialog

import java.time.{ LocalDateTime, ZoneId }

import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import ir.sndu.server.model.dialog.UserDialog
import slick.dbio.Effect
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }

import scala.concurrent.ExecutionContext

object UserDialogRepo {

  val userDialogs = TableQuery[UserDialogTable]

  val byPKC = Compiled(byPK _)
  val idByPeerTypeC = Compiled(idByPeerType _)

  val notArchived = userDialogs

  private def byPK(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    userDialogs.filter(u ⇒ u.userId === userId && u.peerType === peerType && u.peerId === peerId)

  private def byPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    userDialogs.filter(u ⇒ u.userId === userId && u.peerType === peerType)

  private def idByPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    byPeerType(userId, peerType).map(_.peerId)

}

trait UserDialogOperations {
  import UserDialogRepo._

  def createUserDialog(
    userId:               Int,
    peer:                 ApiPeer,
    ownerLastReceivedSeq: Int,
    ownerLastReadSeq:     Int): FixedSqlAction[Int, NoStream, Effect.Write] =
    userDialogs insertOrUpdate UserDialog(
      userId,
      peer,
      ownerLastReceivedSeq,
      ownerLastReadSeq,
      LocalDateTime.now(ZoneId.systemDefault()))

  def findUsersVisible(userId: Rep[Int]) = notArchived.filter(_.userId === userId)

  def findGroupIds(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Effect.Read] =
    idByPeerTypeC((userId, ApiPeerType.ApiPeerType_GROUP.value)).result

  def findUsers(userId: Int, peer: ApiPeer): DBIO[Option[UserDialog]] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).result.headOption

  def usersExists(userId: Int, peer: ApiPeer): FixedSqlAction[Boolean, PostgresProfile.api.NoStream, Effect.Read] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).exists.result

  def updateOwnerLastReceivedSeq(userId: Int, peer: ApiPeer, ownerLastReceivedSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.ownerLastReceivedSeq).update(ownerLastReceivedSeq)

  def updateOwnerLastReadSeq(userId: Int, peer: ApiPeer, ownerLastReadSeq: Int)(implicit ec: ExecutionContext): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.ownerLastReadSeq).update(ownerLastReadSeq)

  def delete(userId: Int, peer: ApiPeer): FixedSqlAction[Int, NoStream, Effect.Write] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).delete

}
