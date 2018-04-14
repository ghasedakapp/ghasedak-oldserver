package ir.sndu.server.rpc.messaging

import java.time.{ Instant, LocalDateTime, ZoneId }

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import com.google.protobuf.CodedInputStream
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.dialog.DialogRepo
import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.messaging.MessagingServiceGrpc.MessagingService
import ir.sndu.server.messaging._
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

  override def sendMessage(request: RequestSendMessage): Future[ResponseVoid] =
    authorize(request.token) { userId =>
      val (outPeer, randomId, message, _) = RequestSendMessage.unapply(request).get

      userExt.sendMessage(
        userId,
        outPeer.map(p => ApiPeer(p.`type`, p.id)).get,
        randomId,
        message.get).map(_ => ResponseVoid())
    }

  override def loadHistory(request: RequestLoadHistory): Future[ResponseLoadHistory] =
    authorize(request.token) { userId =>
      val (peer, date, limit, _) = RequestLoadHistory.unapply(request).get
      db.run(HistoryMessageRepo.findAfter(
        userId,
        ApiPeer(peer.get.`type`, peer.get.id),
        LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()),
        limit)) map { history =>
        history.map(msg => ApiMessageContainer(
          msg.senderUserId,
          msg.randomId,
          msg.date.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli,
          Some(ApiMessage().mergeFrom(CodedInputStream.newInstance(msg.messageContentData)))))

      } map (ResponseLoadHistory(_))
    }

  override def loadDialogs(request: RequestLoadDialogs): Future[ResponseLoadDialogs] =
    authorize(request.token) { userId =>
      db.run(DialogRepo.find(userId, request.limit)) map (r => ResponseLoadDialogs(r.map(_.toApi)))
    }

}
