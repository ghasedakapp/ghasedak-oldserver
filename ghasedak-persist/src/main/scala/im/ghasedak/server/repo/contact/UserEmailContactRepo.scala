package im.ghasedak.server.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.contact.{ UserEmailContact, UserPhoneContact }
import slick.dbio.Effect
import slick.lifted.{ QueryBase, Tag }
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }
import im.ghasedak.server.repo.TypeMapper._
import im.ghasedak.server.repo.contact.UserPhoneContactRepo.pcontacts

final class UserEmailContactTable(tag: Tag) extends UserContactBase[UserEmailContact](tag, "user_email_contacts") with InheritingTable {
  def email = column[String]("email")

  override val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, orgId, localName, deletedAt) <> (UserEmailContact.tupled, UserEmailContact.unapply)
}

object UserEmailContactRepo {
  val econtacts = TableQuery[UserEmailContactTable]

  private def findByEmail(orgId: Int, email: String) =
    econtacts.filter(c ⇒ c.email === email && c.orgId === orgId)

  def exist(orgId: Int, ownerId: Int, email: String): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Effect.Read] =
    econtacts.filter(c ⇒ c.email === email && c.orgId === orgId && c.ownerUserId === ownerId).exists.result

  def find(orgId: Int, email: String): FixedSqlStreamingAction[Seq[UserEmailContact], UserEmailContact, Effect.Read] =
    findByEmail(orgId, email).result

  def findContactUserId(orgId: Int, email: String): FixedSqlStreamingAction[Seq[Int], Int, Effect.Read] =
    findByEmail(orgId, email).map(_.contactUserId).result

  def insertOrUpdate(contact: UserEmailContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    econtacts.insertOrUpdate(contact)

}