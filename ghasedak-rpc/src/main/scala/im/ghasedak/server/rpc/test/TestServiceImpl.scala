package im.ghasedak.server.rpc.test

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.rpc.test.{ TestServicePowerApi, _ }
import im.ghasedak.server.SeqUpdateExtension
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.rpc.RpcErrorHandler
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class TestServiceImpl(implicit system: ActorSystem) extends TestServicePowerApi
  with AuthTokenHelper
  with RpcErrorHandler {

  // todo: use separate dispatcher for rpc handlers
  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  protected val seqUpdateExt = SeqUpdateExtension(system)

  override def checkAuth(request: RequestCheckAuth, metadata: Metadata): Future[ResponseVoid] = {
    authorize(metadata) { _ ⇒
      Future.successful(ResponseVoid())
    }
  }

  override def sendUpdate(request: RequestSendUpdate, metadata: Metadata): Future[ResponseVoid] = {
    authorize(metadata) { clientData ⇒
      seqUpdateExt.deliverUserUpdate(clientData.userId, request.getUpdateContainer)
        .map(_ ⇒ ResponseVoid())
    }
  }

}
