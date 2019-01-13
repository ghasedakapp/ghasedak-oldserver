package im.ghasedak.server

import im.ghasedak.server.update.UpdateMapping
import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.common.schema.{ SchemaInfo, SchemaType }

object UpdateMappingSchema {

  def apply(): UpdateMappingSchema = new UpdateMappingSchema()

}

class UpdateMappingSchema extends Schema[UpdateMapping] {

  override def encode(message: UpdateMapping): Array[Byte] = message.toByteArray

  override def decode(bytes: Array[Byte]): UpdateMapping = UpdateMapping.parseFrom(bytes)

  override def getSchemaInfo: SchemaInfo =
    new SchemaInfo()
      .setName("UpdateMapping")
      .setType(SchemaType.PROTOBUF)
      .setSchema(Array[Byte](0))

}
