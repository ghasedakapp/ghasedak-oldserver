package ir.sndu.server

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.Config
import io.grpc.ServerServiceDefinition
import ir.sndu.rpc.auth.AuthServiceGrpc
import ir.sndu.server.frontend.Frontend
import ir.sndu.server.rpc.auth.AuthServiceImpl

import scala.concurrent.ExecutionContext

object ElitemServerBuilder {

  def start(config: Config): Unit = {
    implicit val system: ActorSystem =
      ActorSystem(config.getString("server-name"), config)

    if (config.getList("akka.cluster.seed-nodes").isEmpty) {
      Cluster(system).join(Cluster(system).selfAddress)
    }

    implicit val ex: ExecutionContext = system.dispatcher

    Frontend.start(ServiceDescriptors.services)(config)
  }

}

object ServiceDescriptors {

  def services(implicit system: ActorSystem, ec: ExecutionContext): Seq[ServerServiceDefinition] = {
    Seq(
      AuthServiceGrpc.bindService(new AuthServiceImpl, ec))
  }

}
