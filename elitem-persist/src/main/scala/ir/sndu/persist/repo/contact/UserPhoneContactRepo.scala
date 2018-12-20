package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UserPhoneContact
import slick.lifted.Tag

final class UserPhoneContactTable(tag: Tag) extends UserContactBaseTable[UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {

  def phoneNumber = column[Long]("phone_number")

  val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, name, isDeleted) <> (UserPhoneContact.tupled, UserPhoneContact.unapply)

}

object UserPhoneContactRepo {

}
