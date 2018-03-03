package ir.sndu.server.frontend

import io.grpc.{ BindableService, Server, ServerBuilder, ServerServiceDefinition }
import org.slf4j.LoggerFactory

object GrpcFrontend {
  private var server: Option[Server] = None
  private val logger = LoggerFactory.getLogger(getClass)
  def start(host: String, port: Int, services: Seq[ServerServiceDefinition]): Unit = {
    val serverBuilder = ServerBuilder.forPort(port)
    services foreach serverBuilder.addService
    server = Some(serverBuilder.build().start)
    logger.info("Server started, listening on " + port)

    sys.addShutdownHook {
      logger.error("*** shutting down gRPC server since JVM is shutting down")
      stop()
      logger.error("*** server shut down")
    }
  }

  private def stop(): Unit =
    server foreach (_.shutdown)

}
