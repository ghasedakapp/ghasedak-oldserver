package im.ghasedak.server.rpc.update

import im.ghasedak.server.rpc.RpcError
import io.grpc._

object UpdateRpcErrors {

  val SeqStateNotFound = RpcError(Status.NOT_FOUND, "SEQ_STATE_NOT_FOUND")

  val InvalidSeqState = RpcError(Status.INTERNAL, "INVALID_SEQ_STATE")

}
