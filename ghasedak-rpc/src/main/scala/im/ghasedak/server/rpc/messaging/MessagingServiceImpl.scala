package im.ghasedak.server.rpc.messaging

import java.time.ZoneOffset

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import com.google.protobuf.CodedInputStream
import im.ghasedak.api.messaging.{ ApiMessage, ApiMessageContainer }
import im.ghasedak.api.peer.ApiPeer
import im.ghasedak.rpc.messaging._
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.dialog.DialogExtension
import im.ghasedak.server.repo.history.HistoryMessageRepo
import im.ghasedak.server.rpc.RpcErrorHandler
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.user.UserExtension
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class MessagingServiceImpl(implicit system: ActorSystem) extends MessagingService
  with AuthTokenHelper
  with MessagingServiceHelper
  with RpcErrorHandler {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)
  protected val dialogExt = DialogExtension(system)

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
      dialogExt.loadDialogs(clientData.userId, request.limit)
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
