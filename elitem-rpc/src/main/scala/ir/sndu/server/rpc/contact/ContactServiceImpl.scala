package ir.sndu.server.rpc.contact

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import im.ghasedak.rpc.contact.ContactServiceGrpc.ContactService
import im.ghasedak.rpc.contact._
import im.ghasedak.rpc.misc.ResponseVoid
import ir.sndu.persist.db.DbExtension
import ir.sndu.persist.repo.contact.UserContactRepo
import ir.sndu.server.rpc.RpcError
import ir.sndu.server.rpc.auth.helper.AuthTokenHelper
import ir.sndu.server.rpc.common.CommonRpcError
import ir.sndu.server.utils.StringUtils._
import ir.sndu.server.utils.concurrent.DBIOResult
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class ContactServiceImpl(implicit system: ActorSystem) extends ContactService
  with AuthTokenHelper
  with ContactServiceHelper
  with DBIOResult[RpcError] {

  override implicit val ec: ExecutionContext = system.dispatcher

  override val db: PostgresProfile.backend.Database = DbExtension(system).db

  override val log: LoggingAdapter = Logging.getLogger(system, this)

  implicit private def onFailure: PartialFunction[Throwable, RpcError] = {
    case rpcError: RpcError ⇒ rpcError
    case ex ⇒
      log.error(ex, "Internal error")
      CommonRpcError.InternalError
  }

  override def getContacts(request: RequestGetContacts): Future[ResponseGetContacts] = {
    authorize { clientData ⇒
      db.run(UserContactRepo.findContactIdsActive(clientData.userId)
        .map(ResponseGetContacts(_)))
    }
  }

  override def addContact(request: RequestAddContact): Future[ResponseAddContact] = {
    authorize { clientData ⇒
      val action: Result[ResponseAddContact] = for {
        localName ← fromOption(CommonRpcError.InvalidName)(validName(request.localName))
        contactRecord ← fromOption(ContactRpcError.InvalidContactRecord)(request.contactRecord)
        contactUserId ← getContactRecordUserId(contactRecord, clientData.orgId)
        _ ← fromBoolean(ContactRpcError.CantAddSelf)(clientData.userId != contactUserId)
        exists ← fromDBIO(UserContactRepo.exists(ownerUserId = clientData.userId, contactUserId = contactUserId))
        _ ← fromBoolean(ContactRpcError.ContactAlreadyExists)(!exists)
        _ ← addUserContact(clientData.userId, contactUserId, localName, contactRecord)
      } yield ResponseAddContact(contactUserId)
      val result = db.run(action.value)
      result
    }
  }

  override def removeContact(request: RequestRemoveContact): Future[ResponseVoid] = {
    authorize { clientData ⇒
      val action: Result[ResponseVoid] = for {
        exists ← fromDBIO(UserContactRepo.exists(clientData.userId, request.contactUserId))
        _ ← fromBoolean(ContactRpcError.ContactNotFound)(exists)
        _ ← fromDBIO(UserContactRepo.delete(clientData.userId, request.contactUserId))
      } yield ResponseVoid()
      val result = db.run(action.value)
      result
    }
  }

  override def searchContacts(request: RequestSearchContacts): Future[ResponseSearchContacts] = ???

}
