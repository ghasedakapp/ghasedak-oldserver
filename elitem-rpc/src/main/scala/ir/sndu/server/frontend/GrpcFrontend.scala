package ir.sndu.server.frontend

import io.grpc.ServerBuilder

object GrpcFrontend {
  def start(host: String, port: Int): Unit = {
    ServerBuilder.forPort(port)
  }
}
