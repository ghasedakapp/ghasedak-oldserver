package im.ghasedak.server.rpc.messaging

import scala.concurrent.Future

trait MessagingServiceHelper {
  this: MessagingServiceImpl ⇒

  protected def withValidChat[T](peer: Long, senderUserId: Int)(f: ⇒ Future[T]): Future[T] = {
    f
    //    if (peer.exists(_.id == senderUserId)) {
    //      log.warning("Attempt to send message to yourself")
    //      Future.failed(MessagingRpcErrors.MessageToSelf)
    //    } else f
  }

}
