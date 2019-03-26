package im.ghasedak.server.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.contact.UserPhoneContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlAction

final class UserPhoneContactTable(tag: Tag) extends UserContactBase[UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")

  override val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, localName, isDeleted) <> (UserPhoneContact.tupled, UserPhoneContact.unapply)
}

object UserPhoneContactRepo {
  val pcontacts = TableQuery[UserPhoneContactTable]

  def insertOrUpdate(contact: UserPhoneContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    pcontacts.insertOrUpdate(contact)

}