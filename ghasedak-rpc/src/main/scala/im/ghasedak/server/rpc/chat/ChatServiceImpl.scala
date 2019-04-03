package im.ghasedak.server.rpc.chat

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import im.ghasedak.rpc.chat.{ ChatServicePowerApi, RequestCreateChat, RequestInviteUser, ResponseCreateChat }
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.chat.ChatExtension
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.dialog.DialogExtension
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.user.UserExtension
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class ChatServiceImpl(implicit system: ActorSystem) extends ChatServicePowerApi
  with AuthTokenHelper {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val userExt = UserExtension(system)
  protected val chatExt = ChatExtension(system)
  protected val dialogExt = DialogExtension(system)

  override def createChat(request: RequestCreateChat, metadata: Metadata): Future[ResponseCreateChat] =
    authorize(metadata) { clientData ⇒
      val (rid, chatType, title, users) = RequestCreateChat.unapply(request).get

      chatExt.createChat(clientData.userId, rid, chatType, title, users)
    }

  override def inviteChat(request: RequestInviteUser, metadata: Metadata): Future[ResponseVoid] =
    authorize(metadata) { clientData ⇒
      val (randomId, chatId, userId) = RequestInviteUser.unapply(request).get

      chatExt.inviteUser(clientData.userId, randomId, chatId, userId)
    }
}