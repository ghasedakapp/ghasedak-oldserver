package ir.sndu.server

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import io.grpc.ServerServiceDefinition
import ir.sndu.server.auth.AuthServiceGrpc
import ir.sndu.server.config.{ AppType, ElitemConfigFactory }
import ir.sndu.server.contacts.ContactServiceGrpc
import ir.sndu.server.frontend.Frontend
import ir.sndu.server.group.GroupExtension
import ir.sndu.server.messaging.MessagingServiceGrpc
import ir.sndu.server.rpc.auth.AuthServiceImpl
import ir.sndu.server.rpc.contacts.ContactServiceImpl
import ir.sndu.server.rpc.group.GroupServiceImpl
import ir.sndu.server.rpc.groups.GroupServiceGrpc
import ir.sndu.server.rpc.messaging.MessagingServiceImpl
import ir.sndu.server.rpc.user.UsersServiceImpl
import ir.sndu.server.rpc.users.UserServiceGrpc

import scala.concurrent.ExecutionContext

object Main extends App {

  val config = ElitemConfigFactory.load(AppType.Server)

  ElitemServer.start()

}

object ElitemServer {
  def start(): Unit = {
    //Register proto here

    implicit val config = ConfigFactory.load()
    implicit val system = ActorSystem(config.getString("server-name"), config)

    if (config.getList("akka.cluster.seed-nodes").isEmpty)
      Cluster(system).join(Cluster(system).selfAddress)

    implicit val ex: ExecutionContext = system.dispatcher

    implicit val groupExt = GroupExtension(system)

    Frontend.start(ServiceDescriptors.services)
  }
}

object ServiceDescriptors {
  def services(implicit system: ActorSystem, ec: ExecutionContext): Seq[ServerServiceDefinition] = {
    Seq(
      AuthServiceGrpc.bindService(new AuthServiceImpl, ec),
      MessagingServiceGrpc.bindService(new MessagingServiceImpl, ec),
      ContactServiceGrpc.bindService(new ContactServiceImpl, ec),
      UserServiceGrpc.bindService(new UsersServiceImpl(), ec),
      GroupServiceGrpc.bindService(new GroupServiceImpl(), ec))
  }
}
