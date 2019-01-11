package im.ghasedak.server.rpc.contact

import io.grpc.Status
import im.ghasedak.server.rpc.RpcError

object ContactRpcErrors {

  val InvalidContactRecord = RpcError(Status.INTERNAL, "INVALID_CONTACT_RECORD")

  val CantAddSelf = RpcError(Status.INTERNAL, "CANT_ADD_SELF")

  val ContactAlreadyExists = RpcError(Status.INTERNAL, "CONTACT_ALREADY_EXISTS")

  val ContactNotFound = RpcError(Status.NOT_FOUND, "CONTACT_NOT_FOUND")

}
