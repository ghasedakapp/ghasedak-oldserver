package ir.sndu.server.rpc.auth

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object AuthRpcErrors {

  val AuthTestError = RpcError(Status.INTERNAL, "AUTH_TEST_ERROR")

  val InvalidApiKey = RpcError(Status.INTERNAL, "INVALID_API_KEY")

  val InvalidPhoneNumber = RpcError(Status.INTERNAL, "INVALID_PHONE_NUMBER")

  val UserIsDeleted = RpcError(Status.INTERNAL, "USER_IS_DELETED")

  val MissingToken = RpcError(Status.UNAUTHENTICATED, "MISSING_TOKEN")

  val InvalidToken = RpcError(Status.UNAUTHENTICATED, "INVALID_TOKEN")

  val AuthCodeExpired = RpcError(Status.INTERNAL, "AUTH_CODE_EXPIRED")

  val NotValidated = RpcError(Status.INTERNAL, "NOT_VALIDATED")

  val InvalidAuthCode = RpcError(Status.INTERNAL, "INVALID_AUTH_CODE")

  val InvalidName = RpcError(Status.INTERNAL, "INVALID_NAME")

}