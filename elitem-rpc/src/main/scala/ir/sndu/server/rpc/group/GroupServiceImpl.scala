package ir.sndu.server.rpc.group

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.server.group.GroupExtension
import ir.sndu.server.rpc.auth.helper.AuthHelper
import ir.sndu.server.rpc.auth.helper.AuthHelper.ClientData
import ir.sndu.server.rpcgroup.GroupServiceGrpc.GroupService
import ir.sndu.server.rpcgroup._
import ir.sndu.server.rpcmisc._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

class GroupServiceImpl(implicit system: ActorSystem) extends GroupService
  with AuthHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = PostgresDb.db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  private val groupExt = GroupExtension(system)

  override def createGroup(request: RequestCreateGroup): Future[ResponseCreateGroup] =
    authorize { clientData: ClientData ⇒
      val (randomId, title, _, typ) = RequestCreateGroup.unapply(request).get
      groupExt.create(typ, clientData.userId, title, randomId, Seq.empty) map (group ⇒
        ResponseCreateGroup(group = Some(group)))
    }

  override def deleteGroup(request: RequestDeleteGroup): Future[ResponseSeq] =
    Future.successful(ResponseSeq())

  override def inviteUser(request: RequestInviteUser): Future[ResponseSeqDate] =
    authorize { clientData: ClientData ⇒
      val (groupPeer, userPeer, _) = RequestInviteUser.unapply(request).get
      groupExt.invite(groupPeer.get.id, userPeer.get.userId, clientData.userId) map (_ ⇒ ResponseSeqDate())
    }

  override def kickUser(request: RequestKickUser): Future[ResponseSeqDate] =
    authorize { _ ⇒
      val (groupPeer, userPeer, _) = RequestKickUser.unapply(request).get
      groupExt.kick(groupPeer.get.id, userPeer.get.userId) map (_ ⇒ ResponseSeqDate())
    }
}
