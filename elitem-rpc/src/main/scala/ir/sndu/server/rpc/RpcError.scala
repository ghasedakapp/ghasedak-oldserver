package ir.sndu.server.rpc

import io.grpc._

case class RpcError(status: Status, description: String)
  extends StatusRuntimeException(status.withDescription(description))
