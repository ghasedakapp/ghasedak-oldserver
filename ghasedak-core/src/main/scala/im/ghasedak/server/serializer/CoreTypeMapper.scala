package im.ghasedak.server.serializer

import java.time.Instant

import scalapb.TypeMapper

case class ActorRefContainer(ref: String)

object CoreTypeMapper {
  private def applyInstant(millis: Long): Instant = Instant.ofEpochMilli(millis)
  private def unapplyInstant(dt: Instant): Long = dt.toEpochMilli

  implicit val actorRefContainer: TypeMapper[String, ActorRefContainer] = TypeMapper(ActorRefContainer.apply)(_.ref)
  implicit val instantMapper: TypeMapper[Long, Instant] = TypeMapper(applyInstant)(unapplyInstant)

}
