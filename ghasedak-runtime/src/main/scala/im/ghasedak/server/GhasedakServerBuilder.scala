package im.ghasedak.server

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.Config
import im.ghasedak.rpc.auth.AuthServiceHandler
import im.ghasedak.rpc.contact.ContactServiceHandler
import im.ghasedak.rpc.messaging.MessagingServiceHandler
import im.ghasedak.rpc.test.TestServiceHandler
import im.ghasedak.rpc.user.UserServiceHandler
import io.grpc.ServerServiceDefinition
import im.ghasedak.rpc.auth.AuthServiceGrpc
import im.ghasedak.rpc.contact.ContactServiceGrpc
import im.ghasedak.rpc.messaging.MessagingServiceGrpc
import im.ghasedak.rpc.test.TestServiceGrpc
import im.ghasedak.rpc.update.UpdateServiceGrpc
import im.ghasedak.rpc.user.UserServiceGrpc
import im.ghasedak.server.frontend.Frontend
import im.ghasedak.server.rpc.auth.AuthServiceImpl
import im.ghasedak.server.rpc.contact.ContactServiceImpl
import im.ghasedak.server.rpc.messaging.MessagingServiceImpl
import im.ghasedak.server.rpc.test.TestServiceImpl
import im.ghasedak.server.rpc.update.UpdateServiceImpl
import im.ghasedak.server.rpc.user.UserServiceImpl
import io.grpc.ServerServiceDefinition

import scala.concurrent.{ ExecutionContext, Future }
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.{ Http, HttpConnectionContext }
import akka.grpc.scaladsl.ServiceHandler

object GhasedakServerBuilder {

  def start(implicit config: Config): ActorSystem = {
    implicit val system: ActorSystem =
      ActorSystem(config.getString("server-name"), config)

    if (config.getList("akka.cluster.seed-nodes").isEmpty) {
      Cluster(system).join(Cluster(system).selfAddress)
    }

    implicit val ex: ExecutionContext = system.dispatcher
    implicit val mat: Materializer = ActorMaterializer()

    Frontend.start(ServiceDescriptors.services)

    system
  }

}

object ServiceDescriptors {

  def services(implicit system: ActorSystem, ec: ExecutionContext, mat: Materializer): HttpRequest ⇒ Future[HttpResponse] = {
    // explicit types not needed but included in example for clarity
    val testService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      TestServiceHandler.partial(new TestServiceImpl)
    val authService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      AuthServiceHandler.partial(new AuthServiceImpl)
    val messagingService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      MessagingServiceHandler.partial(new MessagingServiceImpl)
    val contactService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      ContactServiceHandler.partial(new ContactServiceImpl)
    val userService: PartialFunction[HttpRequest, Future[HttpResponse]] =
      UserServiceHandler.partial(new UserServiceImpl)

    ServiceHandler.concatOrNotFound(
      testService,
      authService,
      messagingService,
      contactService,
      userService
    )
  }

}
