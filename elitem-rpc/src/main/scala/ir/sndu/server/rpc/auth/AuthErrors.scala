package ir.sndu.server.rpc.auth

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object AuthErrors {
  val InvalidToken = RpcError(Status.UNAUTHENTICATED, "INVALID_TOKEN")
}
