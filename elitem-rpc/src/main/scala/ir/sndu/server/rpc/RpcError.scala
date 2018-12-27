package ir.sndu.server.rpc

import io.grpc._

case class RpcError(status: Status, tag: String, description: String) extends StatusRuntimeException(
  status.withDescription(description),
  new MetadataBuilder().put(Constant.TAG_METADATA_KEY, tag).build)

object RpcError {
  def apply(status: Status, tag: String): RpcError = new RpcError(status, tag, null)
}