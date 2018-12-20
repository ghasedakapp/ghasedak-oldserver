package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.contact.UnregisteredEmailContact
import slick.dbio.Effect
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction }

import scala.util.Try

final class UnregisteredEmailContactTable(tag: Tag) extends UnregisteredContactBaseTable[UnregisteredEmailContact](tag, "unregistered_email_contacts") with InheritingTable {

  def email = column[String]("email")

  val inherited: UnregisteredContactTable = UnregisteredContactRepo.ucontacts.baseTableRow

  def pk = primaryKey("unregistered_email_contacts_pkey", (email, ownerUserId))

  def * = (email, ownerUserId, name) <> (UnregisteredEmailContact.tupled, UnregisteredEmailContact.unapply)

}

object UnregisteredEmailContactRepo {

  private val emailContacts = TableQuery[UnregisteredEmailContactTable]

  def create(email: String, ownerUserId: Int, name: Option[String]): FixedSqlAction[Int, NoStream, Effect.Write] =
    emailContacts += UnregisteredEmailContact(email, ownerUserId, name)

  def createIfNotExists(email: String, ownerUserId: Int, name: Option[String]): DBIOAction[Try[Int], NoStream, Effect.Write] = {
    create(email, ownerUserId, name).asTry
  }

  def find(email: String): FixedSqlStreamingAction[Seq[UnregisteredEmailContact], UnregisteredEmailContact, Effect.Read] =
    emailContacts.filter(_.email === email).result

  def deleteAll(email: String): FixedSqlAction[Int, NoStream, Effect.Write] =
    emailContacts.filter(_.email === email).delete
}
