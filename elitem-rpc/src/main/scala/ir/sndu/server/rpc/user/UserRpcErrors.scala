package ir.sndu.server.rpc.user

import io.grpc._
import ir.sndu.server.rpc.RpcError

object UserRpcErrors {

  val UserNotFound = RpcError(Status.NOT_FOUND, "USER_NOT_FOUND")

}
