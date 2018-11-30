package ir.sndu.server.group

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.event.Logging
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.GroupCommand
import ir.sndu.server.GroupCommands.{ Create, DeleteGroup, Invite, Kick }
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext

case object StopOffice extends GroupCommand

object GroupProcessor {

  val ShardingTypeName = EntityTypeKey[GroupCommand]("GroupProcessor")
  val MaxNumberOfShards = 1000

  def shardingBehavior(entityId: String): Behavior[GroupCommand] =
    Behaviors.receive { (ctx, msg) ⇒
      implicit val system = ctx.system.toUntyped
      implicit val ec: ExecutionContext = system.dispatcher
      implicit val db: PostgresProfile.backend.Database = PostgresDb.db
      val log = Logging(system, getClass)
      msg match {
        case c: Create ⇒
          Behaviors.same

        case d: DeleteGroup ⇒
          //          d.replyTo ! SeqState()
          Behaviors.same

        case i: Invite ⇒
          //          i.replyTo ! SeqStateDate()
          Behaviors.same

        case k: Kick ⇒
          //          k.replyTo ! SeqStateDate()
          Behaviors.same

        case StopOffice ⇒
          log.debug("Stopping ......")
          Behaviors.stopped
        case _ ⇒
          Behaviors.unhandled
      }
    }

}
