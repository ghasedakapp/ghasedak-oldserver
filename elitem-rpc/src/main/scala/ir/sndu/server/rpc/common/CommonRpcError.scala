package ir.sndu.server.rpc.common

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object CommonRpcError {

  val InternalError = RpcError(Status.INTERNAL, "INTERNAL_ERROR")

}
