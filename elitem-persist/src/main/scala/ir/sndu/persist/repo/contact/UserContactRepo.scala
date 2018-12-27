package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UserContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }

final class UserContactTable(tag: Tag) extends UserContactBaseTable[UserContact](tag, "user_contacts") {

  def * = (ownerUserId, contactUserId, localName, isDeleted) <> (UserContact.tupled, UserContact.unapply)

}

object UserContactRepo {

  val contacts = TableQuery[UserContactTable]

  private val active = contacts.filter(_.isDeleted === false)

  def findContactIdsActive(ownerUserId: Int): FixedSqlStreamingAction[Seq[Int], Int, Effect.Read] =
    active.filter(_.ownerUserId === ownerUserId).map(_.contactUserId).distinct.result

  def find(ownerUserId: Int): FixedSqlStreamingAction[Seq[UserContact], UserContact, Effect.Read] =
    active.filter(_.ownerUserId === ownerUserId).result

  def exists(ownerUserId: Int, contactUserId: Int): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Effect.Read] =
    active.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId).exists.result

  def insertOrUpdate(contact: UserContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    contacts.insertOrUpdate(contact)

  def delete(ownerUserId: Int, contactUserId: Int): FixedSqlAction[Int, NoStream, Effect.Write] =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === false)
      .map(_.isDeleted).update(true)

}
