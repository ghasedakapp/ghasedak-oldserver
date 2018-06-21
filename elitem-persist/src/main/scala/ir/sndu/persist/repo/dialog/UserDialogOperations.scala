package ir.sndu.persist.repo.dialog

import java.time.{ LocalDateTime, ZoneOffset }

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.server.model.dialog.UserDialog
import ir.sndu.server.peer.{ ApiPeer, ApiPeerType }
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
    userId: Int,
    peer: ApiPeer,
    ownerLastReceivedAt: LocalDateTime,
    ownerLastReadAt: LocalDateTime) =
    userDialogs insertOrUpdate UserDialog(
      userId,
      peer,
      ownerLastReceivedAt,
      ownerLastReadAt,
      LocalDateTime.now(ZoneOffset.UTC),
      false)

  def findUsersVisible(userId: Rep[Int]) = notArchived.filter(_.userId === userId)

  def findGroupIds(userId: Int) =
    idByPeerTypeC((userId, ApiPeerType.Group.value)).result

  def findUsers(userId: Int, peer: ApiPeer): DBIO[Option[UserDialog]] =
    byPKC.applied((userId, peer.`type`.value, peer.id)).result.headOption

  def usersExists(userId: Int, peer: ApiPeer) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).exists.result

  def favourite(userId: Int, peer: ApiPeer) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.isFavourite).update(true)

  def unfavourite(userId: Int, peer: ApiPeer) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.isFavourite).update(false)

  def updateOwnerLastReceivedAt(userId: Int, peer: ApiPeer, ownerLastReceivedAt: LocalDateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt)

  def updateOwnerLastReadAt(userId: Int, peer: ApiPeer, ownerLastReadAt: LocalDateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).map(_.ownerLastReadAt).update(ownerLastReadAt)

  def delete(userId: Int, peer: ApiPeer) =
    byPKC.applied((userId, peer.`type`.value, peer.id)).delete

}
