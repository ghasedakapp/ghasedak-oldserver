package ir.sndu.server.rpc

import io.grpc._

case class RpcError(status: Status, tag: String, description: String) extends StatusRuntimeException(
  status.withDescription(description),
  new MetadataBuilder().put(Constant.TAG_METADATA_KEY, tag).build) {

  def this(status: Status, tag: String) = this(status, tag, null)

}

