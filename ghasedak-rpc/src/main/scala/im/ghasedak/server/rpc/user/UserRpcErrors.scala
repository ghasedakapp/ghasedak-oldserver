package im.ghasedak.server.rpc.user

import io.grpc._
import im.ghasedak.server.rpc.RpcError

object UserRpcErrors {

  val UserNotFound = RpcError(Status.NOT_FOUND, "USER_NOT_FOUND")

}
