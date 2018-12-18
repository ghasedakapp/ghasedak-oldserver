package ir.sndu.server.rpc.messaging

import akka.event.LoggingAdapter
import im.ghasedak.api.peer.ApiPeer

import scala.concurrent.Future

trait MessagingHelper {

  val log: LoggingAdapter

  protected def withValidPeer[T](peer: Option[ApiPeer], senderUserId: Int)(f: ⇒ Future[T]): Future[T] = {
    if (peer.exists(_.id == senderUserId)) {
      log.warning("Attempt to send message to yourself")
      Future.failed(MessagingRpcErrors.MessageToSelf)
    } else f
  }

}
