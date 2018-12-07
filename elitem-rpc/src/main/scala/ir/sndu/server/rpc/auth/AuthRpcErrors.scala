package ir.sndu.server.rpc.auth

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object AuthRpcErrors {

  val MissingToken = RpcError(Status.UNAUTHENTICATED, "MISSING_TOKEN")

  val InvalidToken = RpcError(Status.UNAUTHENTICATED, "INVALID_TOKEN")

}