package im.ghasedak.server.rpc.test

import io.grpc.Status
import im.ghasedak.server.rpc.RpcError

object TestRpcErrors {

  val AuthTestError = RpcError(Status.INTERNAL, "AUTH_TEST_ERROR")

}
