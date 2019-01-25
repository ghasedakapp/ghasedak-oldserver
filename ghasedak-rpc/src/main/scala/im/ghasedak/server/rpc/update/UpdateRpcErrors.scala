package im.ghasedak.server.rpc.update

import im.ghasedak.server.rpc.RpcError
import io.grpc._

object UpdateRpcErrors {

  val SeqStateNotFound = RpcError(Status.INVALID_ARGUMENT, "SEQ_STATE_NOT_FOUND")

}
