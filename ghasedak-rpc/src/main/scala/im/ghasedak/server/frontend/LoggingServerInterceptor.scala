package im.ghasedak.server.frontend

import io.grpc._
import org.slf4j.LoggerFactory

class LoggingServerInterceptor extends ServerInterceptor {

  import io.grpc.ServerCall

  val log = LoggerFactory.getLogger(getClass)

  override def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata, serverCallHandler: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val original = serverCallHandler.startCall(serverCall, metadata)
    return new ForwardingServerCallListener.SimpleForwardingServerCallListener[ReqT](original) {
      override def onMessage(message: ReqT): Unit = {
        log.debug(message.toString)
        super.onMessage(message)
      }
    }
  }

}
