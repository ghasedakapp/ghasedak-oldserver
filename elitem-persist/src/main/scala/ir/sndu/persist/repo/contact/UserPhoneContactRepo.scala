package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UserPhoneContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlAction

final class UserPhoneContactTable(tag: Tag) extends UserContactBaseTable[UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {

  def phoneNumber = column[Long]("phone_number")

  val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, localName, isDeleted) <> (UserPhoneContact.tupled, UserPhoneContact.unapply)

}

object UserPhoneContactRepo {

  val phoneContacts = TableQuery[UserPhoneContactTable]

  def insertOrUpdate(contact: UserPhoneContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    phoneContacts.insertOrUpdate(contact)

}
