package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UserEmailContact
import slick.lifted.Tag

final class UserEmailContactTable(tag: Tag) extends UserContactBaseTable[UserEmailContact](tag, "user_email_contacts") with InheritingTable {

  def email = column[String]("email")

  val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, name, isDeleted) <> (UserEmailContact.tupled, UserEmailContact.unapply)

}

object UserEmailContactRepo {

}
