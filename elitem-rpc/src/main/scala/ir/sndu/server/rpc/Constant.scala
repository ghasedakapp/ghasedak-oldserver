package ir.sndu.server.rpc

import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.{ Context, Metadata }

object Constant {

  val TOKEN_CONTEXT_KEY: Context.Key[String] = Context.key[String]("token")

  val TOKEN_METADATA_KEY: Metadata.Key[String] = Metadata.Key.of("token", ASCII_STRING_MARSHALLER)

  val TAG_METADATA_KEY: Metadata.Key[String] = Metadata.Key.of("tag", ASCII_STRING_MARSHALLER)

}
