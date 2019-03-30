package im.ghasedak.server.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile
import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.contact.UserPhoneContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }
import im.ghasedak.server.repo.TypeMapper._
import im.ghasedak.server.repo.contact.UserEmailContactRepo.econtacts

final class UserPhoneContactTable(tag: Tag) extends UserContactBase[UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")

  override val inherited: UserContactTable = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, orgId, localName, deletedAt) <> (UserPhoneContact.tupled, UserPhoneContact.unapply)
}

object UserPhoneContactRepo {
  val pcontacts = TableQuery[UserPhoneContactTable]

  private def findByPhone(orgId: Int, number: Long) =
    pcontacts.filter(c ⇒ c.phoneNumber === number && c.orgId === orgId)

  def exist(orgId: Int, number: Long): FixedSqlAction[Boolean, ExPostgresProfile.api.NoStream, Effect.Read] =
    findByPhone(orgId, number).exists.result

  def find(orgId: Int, number: Long): SqlAction[Option[UserPhoneContact], NoStream, Effect.Read] =
    findByPhone(orgId, number).result.headOption

  def findContactUserId(orgId: Int, number: Long): FixedSqlStreamingAction[Seq[Int], Int, Effect.Read] =
    findByPhone(orgId, number).map(_.contactUserId).result

  def insertOrUpdate(contact: UserPhoneContact): FixedSqlAction[Int, NoStream, Effect.Write] =
    pcontacts.insertOrUpdate(contact)

}