package im.ghasedak.server.rpc

import im.ghasedak.server.rpc.common.CommonRpcErrors
import org.slf4j.LoggerFactory

trait RpcErrorHandler {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit def onFailure: PartialFunction[Throwable, RpcError] = {
    case rpcError: RpcError ⇒ rpcError
    case ex ⇒
      logger.error("Internal error", ex)
      CommonRpcErrors.InternalError
  }

}
