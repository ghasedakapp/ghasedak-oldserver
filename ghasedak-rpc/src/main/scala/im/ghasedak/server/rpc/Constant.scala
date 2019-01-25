package im.ghasedak.server.rpc

import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.{ Context, Metadata }

object Constant {

  val tokenMetadataKey = "token"

  // todo: remove this in future
  val TOKEN_CONTEXT_KEY: Context.Key[String] = Context.key[String](tokenMetadataKey)

  // todo: remove this in future
  val TOKEN_METADATA_KEY: Metadata.Key[String] = Metadata.Key.of(tokenMetadataKey, ASCII_STRING_MARSHALLER)

}
