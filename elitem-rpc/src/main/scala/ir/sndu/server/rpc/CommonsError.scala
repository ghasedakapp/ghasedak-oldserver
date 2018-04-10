package ir.sndu.server.rpc

import io.grpc.Status

object CommonsError {
  val InternalError = RpcError(Status.INTERNAL, "INTERNAL_ERROR")
}
