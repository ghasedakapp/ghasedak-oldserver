package im.ghasedak.server

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.Config
import im.ghasedak.rpc.auth.AuthServiceGrpc
import im.ghasedak.rpc.contact.ContactServiceGrpc
import im.ghasedak.rpc.messaging.MessagingServiceGrpc
import im.ghasedak.rpc.test.TestServiceGrpc
import im.ghasedak.rpc.user.UserServiceGrpc
import io.grpc.ServerServiceDefinition
import im.ghasedak.server.frontend.Frontend
import im.ghasedak.server.rpc.auth.AuthServiceImpl
import im.ghasedak.server.rpc.contact.ContactServiceImpl
import im.ghasedak.server.rpc.messaging.MessagingServiceImpl
import im.ghasedak.server.rpc.test.TestServiceImpl
import im.ghasedak.server.rpc.user.UserServiceImpl

import scala.concurrent.ExecutionContext

object GhasedakServerBuilder {

  def start(config: Config): ActorSystem = {
    implicit val system: ActorSystem =
      ActorSystem(config.getString("server-name"), config)

    if (config.getList("akka.cluster.seed-nodes").isEmpty) {
      Cluster(system).join(Cluster(system).selfAddress)
    }

    implicit val ex: ExecutionContext = system.dispatcher

    Frontend.start(ServiceDescriptors.services)(config)

    system
  }

}

object ServiceDescriptors {

  def services(implicit system: ActorSystem, ec: ExecutionContext): Seq[ServerServiceDefinition] = {
    Seq(
      TestServiceGrpc.bindService(new TestServiceImpl, ec),
      AuthServiceGrpc.bindService(new AuthServiceImpl, ec),
      MessagingServiceGrpc.bindService(new MessagingServiceImpl, ec),
      ContactServiceGrpc.bindService(new ContactServiceImpl(), ec),
      UserServiceGrpc.bindService(new UserServiceImpl(), ec))
  }

}
