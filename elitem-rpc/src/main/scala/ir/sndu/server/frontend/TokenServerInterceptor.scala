package ir.sndu.server.frontend

import com.typesafe.config._
import io.grpc._
import ir.sndu.server.rpc.auth.AuthRpcErrors._
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper

import scala.collection.JavaConverters._

class TokenServerInterceptor extends ServerInterceptor {

  import io.grpc.ServerCall

  private val config: Config = ConfigFactory.load()

  private val unAuthRequests: Seq[String] = config.getStringList("module.auth.unauth-requests").asScala

  private def NOOP_LISTENER[ReqT] = new ServerCall.Listener[ReqT]() {}

  override def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata, serverCallHandler: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val metadataToken = metadata.get(AuthTokenHelper.TOKEN_METADATA_KEY)
    val requestName = serverCall.getMethodDescriptor.getFullMethodName
    Option(metadataToken) match {
      case Some(token) ⇒
        val context = Context.current().withValue(AuthTokenHelper.TOKEN_CONTEXT_KEY, token)
        Contexts.interceptCall(context, serverCall, metadata, serverCallHandler)
      case None ⇒
        if (unAuthRequests.contains(requestName)) {
          serverCallHandler.startCall(serverCall, metadata)
        } else {
          serverCall.close(MissingToken.status, metadata)
          NOOP_LISTENER
        }
    }
  }

}
