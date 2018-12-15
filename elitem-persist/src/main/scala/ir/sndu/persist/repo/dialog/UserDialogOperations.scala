package ir.sndu.persist.repo.dialog

import java.time.{ LocalDateTime, ZoneId }

import ir.sndu.api.peer.{ ApiPeer, ApiPeerType }
import ir.sndu.server.model.dialog.UserDialog
import slick.jdbc.PostgresProfile.api._

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
    ownerLastReadSeq:     Int) =
    userDialogs insertOrUpdate UserDialog(
      userId,
      peer,
      ownerLastReceivedSeq,
      ownerLastReadSeq,
      LocalDateTime.now(ZoneId.systemDefault()))

  def findUsersVisible(userId: Rep[Int]) = notArchived.filter(_.userId === userId)

  def findGroupIds(userId: Int) =
    idByPeerTypeC((userId, ApiPeerType.GROUP.value)).result

  def findUsers(userId: Int, peer: ApiPeer): DBIO[Option[UserDialog]] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).result.headOption

  def usersExists(userId: Int, peer: ApiPeer) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).exists.result

  def updateownerLastReceivedSeq(userId: Int, peer: ApiPeer, ownerLastReceivedSeq: Int)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.ownerLastReceivedSeq).update(ownerLastReceivedSeq)

  def updateownerLastReadSeq(userId: Int, peer: ApiPeer, ownerLastReadSeq: Int)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.ownerLastReadSeq).update(ownerLastReadSeq)

  def delete(userId: Int, peer: ApiPeer) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).delete

}
