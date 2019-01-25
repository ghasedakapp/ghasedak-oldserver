package im.ghasedak.server.rpc

import akka.grpc.GrpcServiceException
import io.grpc._

case class RpcError(code: Status, description: String) extends GrpcServiceException(
  code.withDescription(description))

object RpcError {
  def apply(status: Status, description: String): RpcError = new RpcError(status, description)
}