package im.ghasedak.server.rpc.common

import io.grpc.Status
import im.ghasedak.server.rpc.RpcError

object CommonRpcErrors {

  val InternalError = RpcError(Status.INTERNAL, "INTERNAL_ERROR")

  val CollectionSizeLimitExceed = RpcError(Status.RESOURCE_EXHAUSTED, "COLLECTION_SIZE_LIMIT_EXCEED")

  val InvalidName = RpcError(Status.INVALID_ARGUMENT, "INVALID_NAME")

}
