package im.ghasedak.server.frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.{ Http, HttpConnectionContext }
import akka.stream.Materializer
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object GrpcFrontend {

  private val logger = LoggerFactory.getLogger(getClass)

  def start(host: String, port: Int, services: HttpRequest ⇒ Future[HttpResponse])(
    implicit
    system: ActorSystem,
    mat:    Materializer): Future[Unit] = {

    implicit val ec = system.dispatcher

    val bound = Http().bindAndHandleAsync(
      services,
      interface = host,
      port = port,
      // Needed until akka-http 10.1.5 see  https://github.com/akka/akka-http/issues/2168
      parallelism = 256,
      connectionContext = HttpConnectionContext(http2 = Always))

    sys.addShutdownHook {
      logger.error("*** shutting down gRPC server since JVM is shutting down")
      Await.result(bound, 10 seconds).terminate(hardDeadline = 3 seconds)
      logger.error("*** server shut down")
    }

    bound.foreach { binding ⇒
      logger.debug("gRPC server bound to: {}", binding.localAddress)
    }

    bound map (_ ⇒ ())
  }

}
