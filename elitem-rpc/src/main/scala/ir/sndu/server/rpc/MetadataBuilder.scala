package ir.sndu.server.rpc

import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc._

class MetadataBuilder() {

  private val trailer = new Metadata()

  def put(key: String, value: String): MetadataBuilder = {
    put(Metadata.Key.of(key, ASCII_STRING_MARSHALLER), value)
    this
  }

  def put[T](key: Metadata.Key[T], value: T): MetadataBuilder = {
    trailer.put(key, value)
    this
  }

  def build: Metadata = trailer

}