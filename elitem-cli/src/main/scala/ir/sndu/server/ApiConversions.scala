package ir.sndu.server

import ir.sndu.server.peer.{ ApiOutPeer, ApiPeer, ApiPeerType, ApiUserOutPeer }

object ApiConversions {
  implicit class RichApiPeer(peer: ApiPeer) {
    def toUserOutPeer: Option[ApiUserOutPeer] =
      peer.`type` match {
        case ApiPeerType.Private => Some(ApiUserOutPeer(peer.id))
        case ApiPeerType.Group => None
      }

    def toOutPeer(userId: Int): ApiOutPeer = ApiOutPeer(peer.`type`, peer.id)
  }
}
