package im.ghasedak.server.repo.dialog

import im.ghasedak.api.peer.{ ApiPeer, ApiPeerType }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ Case, Rep }

trait DialogId {

  def getDialogId(optUserId: Option[Int], peer: ApiPeer): String = (optUserId, peer) match {
    case (Some(userId), ApiPeer(ApiPeerType.PRIVATE, peerUserId)) ⇒
      val userIds = s"${userId}_$peerUserId"
      s"${peer.`type`.value}_$userIds"
    case (_, ApiPeer(ApiPeerType.GROUP, groupId)) ⇒
      s"${peer.`type`.value}_$groupId"
    case _ ⇒ throw new RuntimeException(s"invalid params for dialog id passed, optUserId: $optUserId, peer: $peer")
  }

  def repDialogId(userId: Rep[Int], peerId: Rep[Int], peerType: Rep[Int]): Rep[String] = {
    Case If (peerType === ApiPeerType.PRIVATE.value) Then
      repPrivateDialogId(userId, peerId, peerType) Else
      peerType.asColumnOf[String] ++ "_" ++ peerId.asColumnOf[String]
  }

  private def repPrivateDialogId(userId: Rep[Int], peerId: Rep[Int], peerType: Rep[Int]): Rep[String] = {
    Case If (userId < peerId) Then
      peerType.asColumnOf[String] ++ "_" ++ userId.asColumnOf[String] ++ "_" ++ peerId.asColumnOf[String] Else
      peerType.asColumnOf[String] ++ "_" ++ peerId.asColumnOf[String] ++ "_" ++ userId.asColumnOf[String]
  }

}
