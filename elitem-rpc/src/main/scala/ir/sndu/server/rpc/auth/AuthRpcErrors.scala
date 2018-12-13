package ir.sndu.server.rpc.auth

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object AuthRpcErrors {

  val InvalidApiKey = RpcError(Status.INTERNAL, "INVALID_API_KEY")

  val InvalidPhoneNumber = RpcError(Status.INTERNAL, "INVALID_PHONE_NUMBER")

  val UserIsDeleted = RpcError(Status.INTERNAL, "USER_IS_DELETED")

  val MissingToken = RpcError(Status.UNAUTHENTICATED, "MISSING_TOKEN")

  val InvalidToken = RpcError(Status.UNAUTHENTICATED, "INVALID_TOKEN")

  val PhoneCodeExpired = RpcError(Status.INTERNAL, "PHONE_CODE_EXPIRED")

  val InvalidAuthCode = RpcError(Status.INTERNAL, "INVALID_AUTH_CODE")

}