package ir.sndu.server

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.Config
import im.ghasedak.rpc.auth.AuthServiceGrpc
import im.ghasedak.rpc.contact.ContactServiceGrpc
import im.ghasedak.rpc.messaging.MessagingServiceGrpc
import im.ghasedak.rpc.test.TestServiceGrpc
import io.grpc.ServerServiceDefinition
import ir.sndu.server.frontend.Frontend
import ir.sndu.server.rpc.auth.AuthServiceImpl
import ir.sndu.server.rpc.contact.ContactServiceImpl
import ir.sndu.server.rpc.messaging.MessagingServiceImpl
import ir.sndu.server.rpc.test.TestServiceImpl

import scala.concurrent.ExecutionContext

object ElitemServerBuilder {

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
      ContactServiceGrpc.bindService(new ContactServiceImpl(), ec))
  }

}
