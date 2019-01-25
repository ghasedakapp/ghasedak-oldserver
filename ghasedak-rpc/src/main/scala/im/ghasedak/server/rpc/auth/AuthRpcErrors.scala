package im.ghasedak.server.rpc.auth

import io.grpc.Status
import im.ghasedak.server.rpc.RpcError

object AuthRpcErrors {

  val InvalidApiKey = RpcError(Status.INVALID_ARGUMENT, "INVALID_API_KEY")

  val InvalidPhoneNumber = RpcError(Status.INVALID_ARGUMENT, "INVALID_PHONE_NUMBER")

  val UserIsDeleted = RpcError(Status.FAILED_PRECONDITION, "USER_IS_DELETED")

  val MissingToken = RpcError(Status.UNAUTHENTICATED, "MISSING_TOKEN")

  val InvalidToken = RpcError(Status.UNAUTHENTICATED, "INVALID_TOKEN")

  val AuthCodeExpired = RpcError(Status.INVALID_ARGUMENT, "AUTH_CODE_EXPIRED")

  val NotValidated = RpcError(Status.PERMISSION_DENIED, "NOT_VALIDATED")

  val InvalidAuthCode = RpcError(Status.INVALID_ARGUMENT, "INVALID_AUTH_CODE")

}