package ir.sndu.server.rpc.contact

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object ContactRpcError {

  val InvalidContactRecord = RpcError(Status.INTERNAL, "INVALID_CONTACT_RECORD")

  val CantAddSelf = RpcError(Status.INTERNAL, "CANT_ADD_SELF")

  val ContactAlreadyExists = RpcError(Status.INTERNAL, "CONTACT_ALREADY_EXISTS")

}
