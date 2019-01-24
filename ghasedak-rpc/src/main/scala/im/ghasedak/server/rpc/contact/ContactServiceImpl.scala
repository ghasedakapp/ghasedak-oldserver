package im.ghasedak.server.rpc.contact

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.grpc.scaladsl.Metadata
import im.ghasedak.rpc.contact._
import im.ghasedak.rpc.misc.ResponseVoid
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.repo.contact.UserContactRepo
import im.ghasedak.server.repo.user.UserRepo
import im.ghasedak.server.model.contact.UserContact
import im.ghasedak.server.rpc.RpcError
import im.ghasedak.server.rpc.auth.helper.AuthTokenHelper
import im.ghasedak.server.rpc.common.CommonRpcErrors
import im.ghasedak.server.utils.StringUtils._
import im.ghasedak.server.utils.concurrent.DBIOResult
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class ContactServiceImpl(implicit system: ActorSystem) extends ContactServicePowerApi
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
      CommonRpcErrors.InternalError
  }

  override def getContacts(request: RequestGetContacts, metadata: Metadata): Future[ResponseGetContacts] = {
    authorize(metadata) { clientData ⇒
      db.run(UserContactRepo.findContactIdsActive(clientData.userId)
        .map(ResponseGetContacts(_)))
    }
  }

  override def addContact(request: RequestAddContact, metadata: Metadata): Future[ResponseAddContact] = {
    authorize(metadata) { clientData ⇒
      val action: Result[ResponseAddContact] = for {
        contactRecord ← fromOption(ContactRpcErrors.InvalidContactRecord)(request.contactRecord)
        contactUserId ← getContactRecordUserId(contactRecord, clientData.orgId)
        name ← fromDBIO(UserRepo.find(contactUserId).map(_.map(_.name)))
        localName ← fromOption(CommonRpcErrors.InvalidName)(validName(request.localName.getOrElse(name.get)))
        _ ← fromBoolean(ContactRpcErrors.CantAddSelf)(clientData.userId != contactUserId)
        optExistContact ← fromDBIO(UserContactRepo.find(clientData.userId, contactUserId))
        _ ← fromBoolean(ContactRpcErrors.ContactAlreadyExists) {
          val contact = optExistContact.getOrElse(UserContact(clientData.userId, contactUserId, localName))
          !((contact.hasEmail && contact.hasEmail == contactRecord.contact.isEmail) ||
            (contact.hasPhone && contact.hasPhone == contactRecord.contact.isPhoneNumber))
        }
        // fixme: use UserProcessor actor for concurrency problem
        _ ← if (contactRecord.contact.isPhoneNumber)
          fromDBIO(UserContactRepo.insertOrUpdate(
            UserContact(clientData.userId, contactUserId, localName, hasPhone = true, hasEmail = optExistContact.exists(_.hasEmail))))
        else if (contactRecord.contact.isEmail)
          fromDBIO(UserContactRepo.insertOrUpdate(
            UserContact(clientData.userId, contactUserId, localName, hasEmail = true, hasPhone = optExistContact.exists(_.hasPhone))))
        else throw ContactRpcErrors.InvalidContactRecord
      } yield ResponseAddContact(contactUserId)
      val result = db.run(action.value)
      result
    }
  }

  override def removeContact(request: RequestRemoveContact, metadata: Metadata): Future[ResponseVoid] = {
    authorize(metadata) { clientData ⇒
      val action: Result[ResponseVoid] = for {
        exists ← fromDBIO(UserContactRepo.exists(clientData.userId, request.contactUserId))
        _ ← fromBoolean(ContactRpcErrors.ContactNotFound)(exists)
        _ ← fromDBIO(UserContactRepo.delete(clientData.userId, request.contactUserId))
      } yield ResponseVoid()
      val result = db.run(action.value)
      result
    }
  }

  override def searchContacts(request: RequestSearchContacts, metadata: Metadata): Future[ResponseSearchContacts] = ???

}
