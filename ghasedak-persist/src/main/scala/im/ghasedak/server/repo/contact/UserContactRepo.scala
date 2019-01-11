package im.ghasedak.server.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.contact.UserContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

final class UserContactTable(tag: Tag) extends Table[UserContact](tag, "user_contacts") {

  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)

  def contactUserId = column[Int]("contact_user_id", O.PrimaryKey)

  def localName = column[String]("local_name")

  def hasPhone = column[Boolean]("has_phone", O.Default(false))

  def hasEmail = column[Boolean]("has_email", O.Default(false))

  def isDeleted = column[Boolean]("is_deleted", O.Default(false))

  def * = (ownerUserId, contactUserId, localName, hasPhone, hasEmail, isDeleted) <> (UserContact.tupled, UserContact.unapply)

}

object UserContactRepo {

  val contacts = TableQuery[UserContactTable]

  val active = contacts.filter(_.isDeleted === false)

  def findContactIdsActive(ownerUserId: Int): FixedSqlStreamingAction[Seq[Int], Int, Effect.Read] =
    active.filter(_.ownerUserId === ownerUserId).map(_.contactUserId).distinct.result

  def find(ownerUserId: Int): FixedSqlStreamingAction[Seq[UserContact], UserContact, Effect.Read] =
    active.filter(_.ownerUserId === ownerUserId).result

  def find(ownerUserId: Int, contactUserId: Int): SqlAction[Option[UserContact], NoStream, Effect.Read] =
    active.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId).result.headOption

  def exists(ownerUserId: Int, contactUserId: Int): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Effect.Read] =
    active.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId).exists.result

  def insertOrUpdate(contact: UserContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    contacts.insertOrUpdate(contact)

  def delete(ownerUserId: Int, contactUserId: Int): FixedSqlAction[Int, NoStream, Effect.Write] =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.isDeleted === false)
      .map(_.isDeleted).update(true)

}
