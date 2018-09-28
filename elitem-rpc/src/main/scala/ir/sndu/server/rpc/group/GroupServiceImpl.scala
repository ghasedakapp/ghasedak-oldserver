package ir.sndu.server.rpc.group

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.group.GroupExtension
import ir.sndu.server.misc.{ ResponseSeq, ResponseSeqDate }
import ir.sndu.server.rpc.auth.AuthHelper
import ir.sndu.server.rpc.groups.GroupServiceGrpc.GroupService
import ir.sndu.server.rpc.groups._

import scala.concurrent.Future

class GroupServiceImpl(implicit system: ActorSystem) extends GroupService
  with AuthHelper {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)
  private val groupExt = GroupExtension(system)

  override def createGroup(request: RequestCreateGroup): Future[ResponseCreateGroup] =
    authorize(request.token) { userId ⇒
      val (randomId, title, user, typ, _) = RequestCreateGroup.unapply(request).get
      groupExt.create(typ, userId, title, randomId, Seq.empty) map (group ⇒
        ResponseCreateGroup(group = Some(group)))
    }

  override def deleteGroup(request: RequestDeleteGroup): Future[ResponseSeq] = Future.successful(ResponseSeq())

  override def inviteUser(request: RequestInviteUser): Future[ResponseSeqDate] =
    authorize(request.token) { userId ⇒
      val (groupPeer, userPeer, randomId, _) = RequestInviteUser.unapply(request).get
      groupExt.invite(groupPeer.get.id, userPeer.get.userId, userId) map (_ ⇒ ResponseSeqDate())
    }

  override def kickUser(request: RequestKickUser): Future[ResponseSeqDate] =
    authorize(request.token) { userId ⇒
      val (groupPeer, userPeer, randomId, _) = RequestKickUser.unapply(request).get
      groupExt.kick(groupPeer.get.id, userPeer.get.userId) map (_ ⇒ ResponseSeqDate())
    }
}
