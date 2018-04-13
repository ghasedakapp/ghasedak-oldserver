package ir.sndu.server.rpc.messaging

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.messaging.MessagingServiceGrpc.MessagingService
import ir.sndu.server.messaging.{ RequestSendMessage, ResponseVoid }
import ir.sndu.server.peer.ApiPeer
import ir.sndu.server.rpc.auth.AuthHelper
import ir.sndu.server.user.UserExtension

import scala.concurrent.Future

class MessagingServiceImpl(implicit system: ActorSystem) extends MessagingService
  with AuthHelper {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)
  protected val userExt = UserExtension(system)

  override def sendMessage(request: RequestSendMessage): Future[ResponseVoid] = {
    authorize(request.token) { userId =>
      val (outPeer, randomId, message, _) = RequestSendMessage.unapply(request).get

      userExt.sendMessage(userId, outPeer.map(p => ApiPeer(p.`type`, p.id)).get, randomId, message.get).mapTo[ResponseVoid]
    }
  }
}
