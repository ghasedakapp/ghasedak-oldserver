package im.ghasedak.server.rpc

import akka.grpc.GrpcServiceException
import io.grpc._

case class RpcError(code: Status, tag: String, description: String) extends GrpcServiceException(
  code.withDescription(tag))

object RpcError {
  def apply(status: Status, tag: String): RpcError = new RpcError(status, tag, null)
}