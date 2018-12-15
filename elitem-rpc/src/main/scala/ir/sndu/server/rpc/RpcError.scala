package ir.sndu.server.rpc

import io.grpc._

case class RpcError(status: Status, tag: String)
  extends StatusRuntimeException(status.withDescription(tag))
