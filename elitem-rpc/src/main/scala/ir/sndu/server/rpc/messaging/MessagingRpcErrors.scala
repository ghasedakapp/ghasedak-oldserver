package ir.sndu.server.rpc.messaging

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object MessagingRpcError {
  val MessageToSelf = RpcError(Status.PERMISSION_DENIED, "MESSAGE_TO_SELF")
}
