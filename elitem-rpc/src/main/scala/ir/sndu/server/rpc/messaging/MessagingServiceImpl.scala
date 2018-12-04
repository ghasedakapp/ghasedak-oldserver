package ir.sndu.server.rpc.messaging

import java.time.{ Instant, LocalDateTime, ZoneId }

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import com.google.protobuf.CodedInputStream
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.dialog.DialogRepo
import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.api.message._
import ir.sndu.api.peer._
import ir.sndu.server.rpc.auth.helper.AuthHelper
import ir.sndu.server.rpc.auth.helper.AuthHelper.ClientData
import ir.sndu.rpc.messaging.MessagingServiceGrpc.MessagingService
import ir.sndu.rpc.messaging._
import ir.sndu.server.user.UserExtension
import slick.dbio._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

class MessagingServiceImpl(implicit system: ActorSystem) extends MessagingService
  with AuthHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = PostgresDb.db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)

  override def sendMessage(request: RequestSendMessage): Future[ResponseVoid] =
    authorize { clientData: ClientData ⇒
      val (outPeer, randomId, message) = RequestSendMessage.unapply(request).get
      withValidPeer(outPeer, clientData.userId) {
        userExt.sendMessage(
          clientData.userId,
          outPeer.map(p ⇒ ApiPeer(p.`type`, p.id)).get,
          randomId,
          message.get).map(_ ⇒ ResponseVoid())
      }
    }

  override def loadHistory(request: RequestLoadHistory): Future[ResponseLoadHistory] =
    authorize { clientData: ClientData ⇒
      val (peer, date, limit) = RequestLoadHistory.unapply(request).get
      db.run(HistoryMessageRepo.findBefore(
        clientData.userId,
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
    authorize { clientData: ClientData ⇒
      val action = for {
        dialogs ← DialogRepo.find(clientData.userId, request.limit)
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
