package im.ghasedak.server.rpc.contact

import io.grpc.Status
import im.ghasedak.server.rpc.RpcError

object ContactRpcErrors {

  val InvalidContactRecord = RpcError(Status.INVALID_ARGUMENT, "INVALID_CONTACT_RECORD")

  val CantAddSelf = RpcError(Status.FAILED_PRECONDITION, "CANT_ADD_SELF")

  val ContactAlreadyExists = RpcError(Status.FAILED_PRECONDITION, "CONTACT_ALREADY_EXISTS")

  val ContactNotFound = RpcError(Status.NOT_FOUND, "CONTACT_NOT_FOUND")

}
