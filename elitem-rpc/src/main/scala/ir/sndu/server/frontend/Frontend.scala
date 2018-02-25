package ir.sndu.server.frontend

import com.typesafe.config.Config

import scala.collection.JavaConverters._

object EndpointType {
  def fromConfig(str: String): EndpointTypes.EndpointType = {
    str match {
      case "tcp" => EndpointTypes.Tcp
    }
  }
}

object EndpointTypes {
  sealed trait EndpointType
  case object Tcp extends EndpointType
}

object Endpoint {
  def fromConfig(config: Config): Endpoint = {
    Endpoint(EndpointType.fromConfig(config.getString("type")), config.getString("interface"), config.getInt("port"))
  }

}

case class Endpoint(typ: EndpointTypes.EndpointType, interface: String, port: Int)

object Frontend {

  def start(config: Config) = {
    config.getConfigList("endpoints").asScala.map(Endpoint.fromConfig)
  }

  private def startEndpoint(endpoint: Endpoint): Unit = {
    endpoint.typ match {
      case EndpointTypes.Tcp => GrpcFrontend.start(endpoint.interface, endpoint.port)
    }

  }
}
