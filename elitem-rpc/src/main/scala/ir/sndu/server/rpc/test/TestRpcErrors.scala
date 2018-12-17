package ir.sndu.server.rpc.test

import io.grpc.Status
import ir.sndu.server.rpc.RpcError

object TestRpcErrors {

  val AuthTestError = RpcError(Status.INTERNAL, "AUTH_TEST_ERROR")

}
