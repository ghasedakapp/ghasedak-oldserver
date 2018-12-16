package ir.sndu.server.messaging

import java.util.concurrent.Executor

import io.grpc.{ Attributes, CallCredentials, MethodDescriptor, Status }
import io.grpc.CallCredentials.ATTR_SECURITY_LEVEL._

class TokenCallCredential(token: String) extends CallCredentials {
  override def applyRequestMetadata(methodDescriptor: MethodDescriptor[_, _], attributes: Attributes, executor: Executor, metadataApplier: CallCredentials.MetadataApplier): Unit = {
    executor.execute(() ⇒ {
      import io.grpc.Metadata
      try {
        val headers = new Metadata
        val jwtKey = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER)
        headers.put(jwtKey, token)
        metadataApplier.apply(headers)
      } catch {
        case e: Throwable ⇒
          metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e))
      }
    })
  }

  override def thisUsesUnstableApi(): Unit = ()
}
