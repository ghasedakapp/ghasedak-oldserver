package ir.sndu.server.group

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import ir.sndu.persist.db.PostgresDb
import ir.sndu.struct.GroupCommand
import ir.sndu.struct.GroupCommands._
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext

case object StopOffice extends GroupCommand

object GroupProcessor {

  val ShardingTypeName: EntityTypeKey[GroupCommand] = EntityTypeKey[GroupCommand]("GroupProcessor")
  val MaxNumberOfShards = 1000

  def shardingBehavior(entityId: String): Behavior[GroupCommand] =
    Behaviors.receive { (ctx, msg) ⇒
      implicit val system: ActorSystem = ctx.system.toUntyped
      implicit val ec: ExecutionContext = system.dispatcher
      implicit val db: PostgresProfile.backend.Database = PostgresDb.db
      val log = Logging(system, getClass)
      msg match {
        case _: Create ⇒
          Behaviors.same
        case _: DeleteGroup ⇒
          Behaviors.same
        case _: Invite ⇒
          Behaviors.same
        case _: Kick ⇒
          Behaviors.same
        case StopOffice ⇒
          log.debug("Stopping ......")
          Behaviors.stopped
        case _ ⇒
          Behaviors.unhandled
      }
    }

}
