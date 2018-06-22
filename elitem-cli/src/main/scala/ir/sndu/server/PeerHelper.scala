package ir.sndu.server

import ir.sndu.server.peer.{ ApiPeer, ApiPeerType }

object PeerHelper {
  def toUniqueId(peer: ApiPeer): Long = peer.id.toLong * Math.pow(2, 32).toLong + peer.`type`.value
  def fromUniqueId(uniqueId: Long): ApiPeer = {
    val peerType = uniqueId & Int.MaxValue
    val peerId = uniqueId >>> 32
    ApiPeer(ApiPeerType.fromValue(peerType.toInt), peerId.toInt)
  }
}
