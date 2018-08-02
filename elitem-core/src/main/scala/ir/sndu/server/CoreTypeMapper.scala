package ir.sndu.server
import scalapb.TypeMapper

case class ActorRefContainer(ref: String)

object CoreTypeMapper {
  implicit val actorRefContainer: TypeMapper[String, ActorRefContainer] = TypeMapper(ActorRefContainer.apply)(_.ref)
}
