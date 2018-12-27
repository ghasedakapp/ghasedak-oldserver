package ir.sndu.server.rpc.common

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object CommonRpcError {

  val InternalError = RpcError(Status.INTERNAL, "INTERNAL_ERROR")

  val CollectionSizeLimit = RpcError(Status.PERMISSION_DENIED, "COLLECTION_SIZE_LIMIT", "Collection size is more than limit")

  val InvalidName = RpcError(Status.INTERNAL, "INVALID_NAME")

}
