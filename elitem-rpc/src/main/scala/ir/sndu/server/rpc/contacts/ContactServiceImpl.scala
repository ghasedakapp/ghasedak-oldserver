package ir.sndu.server.rpc.contacts

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.user.UserPhoneRepo
import ir.sndu.api.peer._
import ir.sndu.server.rpc.auth.helper.AuthHelper
import ir.sndu.rpc.contact.ContactServiceGrpc.ContactService
import ir.sndu.rpc.contact._
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

class ContactServiceImpl(implicit system: ActorSystem) extends ContactService
  with AuthHelper {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = PostgresDb.db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  override def searchContacts(request: RequestSearchContacts): Future[ResponseSearchContacts] =
    authorize { _ ⇒
      db.run(UserPhoneRepo.findByNumbers(Set(request.query.toLong))).map(users ⇒
        ResponseSearchContacts(users.map(u ⇒ ApiOutPeer(ApiPeerType.Private, u.userId))))
    }

}
