package ir.sndu.server.rpc.contacts

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import ir.sndu.persist.db.PostgresDb
import ir.sndu.persist.repo.user.UserPhoneRepo
import ir.sndu.server.contacts.ContactServiceGrpc.ContactService
import ir.sndu.server.contacts.{ RequestSearchContacts, ResponseSearchContacts }
import ir.sndu.server.peer.{ ApiOutPeer, ApiPeerType }
import ir.sndu.server.rpc.auth.AuthHelper

import scala.concurrent.Future

class ContactServiceImpl(implicit system: ActorSystem) extends ContactService
  with AuthHelper {
  implicit protected val ec = system.dispatcher
  implicit protected val db = PostgresDb.db
  implicit protected val log: LoggingAdapter = Logging.getLogger(system, this)

  override def searchContacts(request: RequestSearchContacts): Future[ResponseSearchContacts] =
    authorize(request.token) { userId =>
      db.run(UserPhoneRepo.findByNumbers(Set(request.query.toLong))).map(users =>
        ResponseSearchContacts(users.map(u => ApiOutPeer(ApiPeerType.Private, u.userId))))
    }

}
