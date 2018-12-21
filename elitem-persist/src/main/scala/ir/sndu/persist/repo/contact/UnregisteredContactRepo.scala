package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.{ UnregisteredContact, UserContact }
import slick.lifted.Tag

final class UnregisteredContactTable(tag: Tag) extends UnregisteredContactBaseTable[UnregisteredContact](tag, "unregistered_contacts") {

  def * = (ownerUserId, localName) <> (UnregisteredContact.tupled, UnregisteredContact.unapply)

}

object UnregisteredContactRepo {

  val ucontacts = TableQuery[UnregisteredContactTable]

}
