package im.ghasedak.server.rpc.messaging

import io.grpc.Status
import im.ghasedak.server.rpc.RpcError

object MessagingRpcErrors {

  val MessageToSelf = RpcError(Status.PERMISSION_DENIED, "MESSAGE_TO_SELF")

  val InvalidChat = RpcError(Status.INVALID_ARGUMENT, "INVALID_CHAT")

}
