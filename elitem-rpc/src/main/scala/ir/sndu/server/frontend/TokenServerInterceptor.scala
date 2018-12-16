package ir.sndu.server.frontend

import io.grpc._
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper

class TokenServerInterceptor extends ServerInterceptor {

  import io.grpc.ServerCall

  private def NOOP_LISTENER[ReqT] = new ServerCall.Listener[ReqT]() {}
  override def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata, serverCallHandler: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val token = metadata.get(AuthTokenHelper.TOKEN_METADATA_KEY)
    //    if (token == null) {
    //      serverCall.close(Status.UNAUTHENTICATED.withDescription("Token is missing from Metadata"), metadata)
    //      NOOP_LISTENER
    //    }
    try {
      //TODO validate token
      val ctx = Context.current().withValue(AuthTokenHelper.TOKEN_CTX_KEY, token)
      Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
    } catch {
      case e: Throwable ⇒
        serverCall.close(Status.UNAUTHENTICATED.withDescription(e.getMessage).withCause(e), metadata)
        NOOP_LISTENER
    }

  }
}
