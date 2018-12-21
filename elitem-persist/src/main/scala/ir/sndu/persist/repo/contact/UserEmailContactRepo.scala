package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UserEmailContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.FixedSqlAction

final class UserEmailContactTable(tag: Tag) extends UserContactBaseTable[UserEmailContact](tag, "user_email_contacts") with InheritingTable {

  def email = column[String]("email")

  val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, localName, isDeleted) <> (UserEmailContact.tupled, UserEmailContact.unapply)

}

object UserEmailContactRepo {

  private val emailContacts = TableQuery[UserEmailContactTable]

  def insertOrUpdate(contact: UserEmailContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    emailContacts.insertOrUpdate(contact)

}
