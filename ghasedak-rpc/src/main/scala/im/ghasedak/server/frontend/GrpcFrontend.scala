package im.ghasedak.server.frontend

import akka.actor.ActorSystem
import io.grpc._
import org.slf4j.LoggerFactory
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.{ Http, HttpConnectionContext }
import akka.stream.Materializer

import scala.concurrent.{ ExecutionContext, Future }

object GrpcFrontend {

  private var server: Option[Server] = None
  private val logger = LoggerFactory.getLogger(getClass)

  def start(host: String, port: Int, services: HttpRequest ⇒ Future[HttpResponse])(
    implicit
    system: ActorSystem,
    mat:    Materializer): Future[Http.ServerBinding] = {
    implicit val ec = system.dispatcher

    val bound = Http().bindAndHandleAsync(
      services,
      interface = host,
      port = port,
      connectionContext = HttpConnectionContext(http2 = Always))

    //    val serverBuilder = ServerBuilder.forPort(port)
    //
    //    services foreach serverBuilder.addService
    //
    //    serverBuilder.intercept(new LoggingServerInterceptor).intercept(new TokenServerInterceptor)
    //
    //    server = Some(serverBuilder.build().start)
    //    logger.info("Server started, listening on " + port)
    //
    sys.addShutdownHook {
      logger.error("*** shutting down gRPC server since JVM is shutting down")
      stop()
      logger.error("*** server shut down")
    }

    bound.foreach { binding ⇒
      println(s"gRPC server bound to: ${binding.localAddress}")
    }

    bound

  }

  private def stop(): Unit = {
    server foreach (_.shutdown)
  }

}
