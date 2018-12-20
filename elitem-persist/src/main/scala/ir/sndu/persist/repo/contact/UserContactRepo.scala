package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UserContact
import slick.lifted.Tag

final class UserContactTable(tag: Tag) extends UserContactBaseTable[UserContact](tag, "user_contacts") {

  def * = (ownerUserId, contactUserId, name, isDeleted) <> (UserContact.tupled, UserContact.unapply)

}

object UserContactRepo {

  val contacts = TableQuery[UserContactTable]

}
