package ir.sndu.server.user

import akka.actor.{ Props, Status }
import akka.pattern.pipe
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.Processor
import ir.sndu.api.peer._
import ir.sndu.struct.UserCommands.SendMessage
import slick.jdbc.PostgresProfile.backend._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

object UserProcessor {
  def props = Props(new UserProcessor())
}

class UserProcessor extends Processor
  with UserCommandHandler
  with UserQueryHandler {

  type CommandHandler = PartialFunction[Any, () ⇒ Future[Any]]

  protected val userId: Int = self.path.name.toInt
  protected val selfPeer = ApiPeer(ApiPeerType.Private, userId)
  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val db: Database = PostgresDb.db

  override def receive: Receive = commandHandler andThen {
    handler ⇒
      Try(
        handler() pipeTo sender).failed map (e ⇒ sender ! Status.Failure(e))
  }

  private def commandHandler: CommandHandler = {
    case sm: SendMessage ⇒ sendMessage(sm)
  }

}
