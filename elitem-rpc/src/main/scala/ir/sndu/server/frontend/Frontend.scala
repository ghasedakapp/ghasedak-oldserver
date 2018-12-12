package ir.sndu.server.frontend

import com.typesafe.config.Config
import io.grpc.ServerServiceDefinition

import scala.collection.JavaConverters._

object EndpointType {
  def fromConfig(str: String): EndpointTypes.EndpointType = {
    str match {
      case "grpc" ⇒ EndpointTypes.Grpc
    }
  }
}

object EndpointTypes {
  sealed trait EndpointType
  case object Grpc extends EndpointType
}

object Endpoint {
  def fromConfig(config: Config): Endpoint = {
    Endpoint(EndpointType.fromConfig(config.getString("type")), config.getString("interface"), config.getInt("port"))
  }

}

case class Endpoint(typ: EndpointTypes.EndpointType, interface: String, port: Int)

object Frontend {

  def start(services: Seq[ServerServiceDefinition])(implicit config: Config): Unit = {
    config.getConfigList("endpoints").asScala.map(Endpoint.fromConfig) foreach (startEndpoint(_, services))
  }

  private def startEndpoint(endpoint: Endpoint, services: Seq[ServerServiceDefinition]): Unit = {
    endpoint.typ match {
      case EndpointTypes.Grpc ⇒ GrpcFrontend.start(endpoint.interface, endpoint.port, services)
    }
  }

}
