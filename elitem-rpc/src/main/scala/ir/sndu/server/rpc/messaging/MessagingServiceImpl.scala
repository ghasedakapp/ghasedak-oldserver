package ir.sndu.server.rpc.messaging

import java.time.{ Instant, LocalDateTime, ZoneId, ZoneOffset }

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import com.google.protobuf.CodedInputStream
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.dialog.DialogRepo
import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.message.{ ApiMessage, ApiMessageContainer }
import ir.sndu.server.messaging.MessagingServiceGrpc.MessagingService
import ir.sndu.server.messaging._
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeer }
import ir.sndu.server.rpc.auth.AuthHelper
import ir.sndu.server.user.UserExtension
import slick.dbio._

import scala.concurrent.Future
class MessagingServiceImpl(implicit system: ActorSystem) extends MessagingService
  with AuthHelper {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)
  protected val userExt = UserExtension(system)

  override def sendMessage(request: RequestSendMessage): Future[ResponseVoid] =
    authorize(request.token) { userId ⇒
      val (outPeer, randomId, message, _) = RequestSendMessage.unapply(request).get

      withValidPeer(outPeer, userId) {
        userExt.sendMessage(
          userId,
          outPeer.map(p ⇒ ApiPeer(p.`type`, p.id)).get,
          randomId,
          message.get).map(_ ⇒ ResponseVoid())
      }

    }

  override def loadHistory(request: RequestLoadHistory): Future[ResponseLoadHistory] =
    authorize(request.token) { userId ⇒
      val (peer, date, limit, _) = RequestLoadHistory.unapply(request).get
      db.run(HistoryMessageRepo.findBefore(
        userId,
        ApiPeer(peer.get.`type`, peer.get.id),
        LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()),
        limit)) map { history ⇒
        history.map(msg ⇒ ApiMessageContainer(
          msg.senderUserId,
          msg.randomId,
          msg.date.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli,
          Some(ApiMessage().mergeFrom(CodedInputStream.newInstance(msg.messageContentData)))))

      } map (ResponseLoadHistory(_))
    }

  override def loadDialogs(request: RequestLoadDialogs): Future[ResponseLoadDialogs] =
    authorize(request.token) { userId ⇒
      val action = for {
        dialogs ← DialogRepo.find(userId, request.limit)
        fullDialogs ← DBIO.sequence(dialogs.map(d ⇒
          HistoryMessageRepo.find(d.userId, d.peer, Some(d.lastMessageDate), 1).headOption.map(d.toApi)))

      } yield ResponseLoadDialogs(fullDialogs)

      db.run(action)
    }

  private def withValidPeer[T](peer: Option[ApiOutPeer], senderUserId: Int)(f: ⇒ Future[T]): Future[T] = {
    if (peer.exists(_.id == senderUserId)) {
      log.warning("Attempt to send message to yourself")
      Future.failed(MessagingError.MessageToSelf)
    } else f
  }
}
