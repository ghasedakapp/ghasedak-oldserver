package ir.sndu.server

import java.io.File

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import io.grpc.ServerServiceDefinition
import ir.sndu.server.auth.AuthServiceGrpc
import ir.sndu.server.frontend.Frontend
import ir.sndu.server.rpc.auth.AuthServiceImpl

import scala.concurrent.ExecutionContext

object Main extends App {

  val configPath = new File(".").getCanonicalPath + "/conf/server.conf"

  System.setProperty("config.file", configPath.toString)

  System.setProperty("file.encoding", "UTF-8")

  //Register proto here

  implicit val config = ConfigFactory.load()
  implicit val system = ActorSystem(config.getString("server-name"), config)

  if (config.getList("akka.cluster.seed-nodes").isEmpty)
    Cluster(system).join(Cluster(system).selfAddress)

  implicit val ex: ExecutionContext = system.dispatcher

  Frontend.start(ServiceDescriptors.services)

}

object ServiceDescriptors {
  def services(implicit system: ActorSystem, ec: ExecutionContext): Seq[ServerServiceDefinition] = {
    Seq(
      AuthServiceGrpc.bindService(new AuthServiceImpl, ec))
  }
}
