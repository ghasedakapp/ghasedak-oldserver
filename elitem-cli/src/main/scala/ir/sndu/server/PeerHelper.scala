package ir.sndu.server

import ir.sndu.server.peer.{ ApiPeer, ApiPeerType }

object PeerHelper {
  def toUniqueId(peer: ApiPeer): Long = (peer.id * Math.pow(2, 32) + peer.`type`.value).toLong
  def fromUniqueId(uniqueId: Long): ApiPeer = {
    val peerType = uniqueId & Int.MaxValue
    val peerId = uniqueId >>> 32
    ApiPeer(ApiPeerType.fromValue(peerType.toInt), peerId.toInt)
  }
}
