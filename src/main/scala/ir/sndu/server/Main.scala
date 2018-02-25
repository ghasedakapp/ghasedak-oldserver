package ir.sndu.server

import java.io.File

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import io.grpc.ServerBuilder

object Main extends App {

  val configPath = new File(".").getCanonicalPath + "/conf/server.conf"

  System.setProperty("config.file", configPath.toString)

  System.setProperty("file.encoding", "UTF-8")

  //Register proto here

  val config = ConfigFactory.load()
  implicit val system = ActorSystem(config.getString("server-name"), config)

  if (config.getList("akka.cluster.seed-nodes").isEmpty)
    Cluster(system).join(Cluster(system).selfAddress)

}
