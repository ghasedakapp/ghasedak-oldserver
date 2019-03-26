package im.ghasedak.server.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.contact.UserEmailContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlAction

final class UserEmailContactTable(tag: Tag) extends UserContactBase[UserEmailContact](tag, "user_email_contacts") with InheritingTable {
  def email = column[String]("email")

  override val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, localName, isDeleted) <> (UserEmailContact.tupled, UserEmailContact.unapply)
}

object UserEmailContactRepo {
  val econtacts = TableQuery[UserEmailContactTable]

  def insertOrUpdate(contact: UserEmailContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    econtacts.insertOrUpdate(contact)

}