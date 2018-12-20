package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UnregisteredPhoneContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }

import scala.util.Try

final class UnregisteredPhoneContactTable(tag: Tag) extends UnregisteredContactBaseTable[UnregisteredPhoneContact](tag, "unregistered_phone_contacts") with InheritingTable {

  def phoneNumber = column[Long]("phone_number")

  val inherited: UnregisteredContactTable = UnregisteredContactRepo.ucontacts.baseTableRow

  def pk = primaryKey("unregistered_phone_contacts_pkey", (phoneNumber, ownerUserId))

  def * = (phoneNumber, ownerUserId, name) <> (UnregisteredPhoneContact.tupled, UnregisteredPhoneContact.unapply)

}

object UnregisteredPhoneContactRepo {

  private val phoneContacts = TableQuery[UnregisteredPhoneContactTable]

  def create(phoneNumber: Long, ownerUserId: Int, name: Option[String]): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneContacts += UnregisteredPhoneContact(phoneNumber, ownerUserId, name)

  def createIfNotExists(phoneNumber: Long, ownerUserId: Int, name: Option[String]): DBIOAction[Try[Int], NoStream, Effect.Write] = {
    create(phoneNumber, ownerUserId, name).asTry
  }

  def find(phoneNumber: Long): FixedSqlStreamingAction[Seq[UnregisteredPhoneContact], UnregisteredPhoneContact, Effect.Read] =
    phoneContacts.filter(_.phoneNumber === phoneNumber).result

  def deleteAll(phoneNumber: Long): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneContacts.filter(_.phoneNumber === phoneNumber).delete

}
