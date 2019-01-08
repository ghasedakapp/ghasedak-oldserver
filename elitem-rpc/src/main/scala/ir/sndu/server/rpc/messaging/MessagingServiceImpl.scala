package ir.sndu.server.rpc.messaging

import java.time.ZoneOffset

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import com.google.protobuf.CodedInputStream
import im.ghasedak.api.messaging.{ ApiMessage, ApiMessageContainer }
import im.ghasedak.api.peer.ApiPeer
import im.ghasedak.rpc.messaging.MessagingServiceGrpc.MessagingService
import im.ghasedak.rpc.messaging._
import im.ghasedak.rpc.misc.ResponseVoid
import ir.sndu.persist.db.DbExtension
import ir.sndu.persist.repo.dialog.DialogRepo
import ir.sndu.persist.repo.history.HistoryMessageRepo
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper
import ir.sndu.server.user.UserExtension
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class MessagingServiceImpl(implicit system: ActorSystem) extends MessagingService
  with AuthTokenHelper
  with MessagingServiceHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)

  override def sendMessage(request: RequestSendMessage): Future[ResponseSendMessage] =
    authorize { clientData ⇒
      val (peer, randomId, message) = RequestSendMessage.unapply(request).get
      withValidPeer(peer, clientData.userId) {
        userExt.sendMessage(
          clientData.userId,
          peer.get,
          randomId,
          message.get)
      }
    }

  override def loadDialogs(request: RequestLoadDialogs): Future[ResponseLoadDialogs] =
    authorize { clientData ⇒
      val action = for {
        dialogs ← DialogRepo.find(clientData.userId, request.limit)
        fullDialogs ← DBIO.sequence(dialogs.map(d ⇒
          HistoryMessageRepo.find(d.userId, d.peer, Some(d.lastMessageDate), 1).headOption.map(d.toApi)))
      } yield ResponseLoadDialogs(fullDialogs)
      db.run(action)
    }

  override def loadHistory(request: RequestLoadHistory): Future[ResponseLoadHistory] =
    authorize { clientData ⇒
      val (peer, seq, _, limit) = RequestLoadHistory.unapply(request).get
      db.run(HistoryMessageRepo.findBefore(
        clientData.userId,
        ApiPeer(peer.get.`type`, peer.get.id),
        seq,
        limit)) map { history ⇒
        history.map(msg ⇒ ApiMessageContainer(
          msg.senderUserId,
          msg.sequenceNr,
          msg.date.atZone(ZoneOffset.UTC).toInstant.toEpochMilli,
          Some(ApiMessage().mergeFrom(CodedInputStream.newInstance(msg.messageContentData)))))
      } map (ResponseLoadHistory(_))
    }

  override def messageReceived(request: RequestMessageReceived): Future[ResponseVoid] =
    authorize { clientData ⇒
      val (peer, seq) = RequestMessageReceived.unapply(request).get
      userExt.messageReceived(clientData.userId, peer.getOrElse(throw MessagingRpcErrors.InvalidPeer), seq)
    }

  override def messageRead(request: RequestMessageRead): Future[ResponseVoid] =
    authorize { clientData ⇒
      val (peer, seq) = RequestMessageRead.unapply(request).get
      userExt.messageRead(clientData.userId, peer.getOrElse(throw MessagingRpcErrors.InvalidPeer), seq)
    }
}
