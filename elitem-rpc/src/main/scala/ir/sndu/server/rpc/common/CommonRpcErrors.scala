package ir.sndu.server.rpc.common

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object CommonRpcErrors {

  val InternalError = RpcError(Status.INTERNAL, "INTERNAL_ERROR")

  val CollectionSizeLimitExceed = RpcError(Status.INTERNAL, "COLLECTION_SIZE_LIMIT_EXCEED")

  val InvalidName = RpcError(Status.INTERNAL, "INVALID_NAME")

}
