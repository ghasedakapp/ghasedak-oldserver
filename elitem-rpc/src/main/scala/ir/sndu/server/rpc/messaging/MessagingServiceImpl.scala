package ir.sndu.server.rpc.messaging

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.messaging.MessagingServiceGrpc.MessagingService
import ir.sndu.server.messaging.{ RequestSendMessage, ResponseVoid }
import ir.sndu.server.rpc.auth.AuthHelper

import scala.concurrent.Future

class MessagingServiceImpl(implicit system: ActorSystem) extends MessagingService with AuthHelper {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)
  override def sendMessage(request: RequestSendMessage): Future[ResponseVoid] = {
    authenticate(request.token) { userId =>
      Future.successful(ResponseVoid())
    }
  }
}
