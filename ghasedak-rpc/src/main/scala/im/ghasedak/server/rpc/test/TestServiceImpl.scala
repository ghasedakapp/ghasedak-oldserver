package im.ghasedak.server.rpc.test

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import im.ghasedak.server.db.DbExtension
import im.ghasedak.rpc.test.{ RequestTestAuth, ResponseTestAuth, TestService, TestServicePowerApi }
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class TestServiceImpl(implicit system: ActorSystem) extends TestServicePowerApi
  with AuthTokenHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  override def testAuth(request: RequestTestAuth, metadata: Metadata): Future[ResponseTestAuth] = {
    authorize(metadata) { _ ⇒
      if (request.exception) {
        Future.failed(TestRpcErrors.AuthTestError)
      } else {
        Future.successful(ResponseTestAuth(true))
      }
    }
  }

}
