package ir.sndu.server.rpc.user

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object UserRpcErrors {
  val LoadUserLimit = RpcError(Status.NOT_FOUND, "LOUD_USER_LIMIT")

}
