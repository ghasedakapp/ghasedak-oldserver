package im.ghasedak.server.repo.contact

import java.time.{ LocalDateTime, ZoneOffset }

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import im.ghasedak.server.model.contact.UserContactModel
import im.ghasedak.server.repo.TypeMapper._
import slick.dbio.Effect
import slick.dbio.Effect.Write
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

import scala.concurrent.ExecutionContext

abstract class UserContactBase[T](tag: Tag, tname: String) extends Table[T](tag, tname) {
  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)
  def contactUserId = column[Int]("contact_user_id", O.PrimaryKey)
  def orgId = column[Int]("org_id", O.PrimaryKey)
  def localName = column[Option[String]]("local_name")
  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

}

final class UserContactTable(tag: Tag) extends UserContactBase[UserContactModel](tag, "user_contacts") {
  def * = (ownerUserId, contactUserId, orgId, localName, deletedAt) <> (UserContactModel.tupled, UserContactModel.unapply)
}

object UserContactRepo {
  val contacts = TableQuery[UserContactTable]
  val active = contacts.filter(_.deletedAt.isEmpty)

  private def byOwnerUserIdNotDeleted(ownerUserId: Rep[Int]) =
    active.filter(_.ownerUserId === ownerUserId)

  private val byOwnerUserIdNotDeletedC = Compiled(byOwnerUserIdNotDeleted _)

  private val countC = Compiled { (userId: Rep[Int]) ⇒
    byOwnerUserIdNotDeleted(userId).length
  }

  def byPKNotDeleted(ownerUserId: Rep[Int], contactUserId: Rep[Int]) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.deletedAt.isEmpty)

  val nameByPKNotDeletedC = Compiled(
    (ownerUserId: Rep[Int], contactUserId: Rep[Int]) ⇒
      byPKNotDeleted(ownerUserId, contactUserId) map (_.localName))

  def byContactUserId(contactUserId: Rep[Int]) = active.filter(_.contactUserId === contactUserId)
  val byContactUserIdC = Compiled(byContactUserId _)

  def byPKDeleted(ownerUserId: Int, contactUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId && c.deletedAt.isDefined)

  private def existsC = Compiled { (ownerUserId: Rep[Int], contactUserId: Rep[Int]) ⇒
    byPKNotDeleted(ownerUserId, contactUserId).exists
  }

  def fetchAll = active.result

  def exists(ownerUserId: Int, contactUserId: Int) = existsC((ownerUserId, contactUserId)).result

  def find(ownerUserId: Int, contactUserId: Int): DBIO[Option[UserContactModel]] =
    byPKNotDeleted(ownerUserId, contactUserId).result.headOption

  def count(ownerUserId: Int) = countC(ownerUserId).result

  def findIds(ownerUserId: Int, contactUserIds: Set[Int]) =
    byOwnerUserIdNotDeletedC.applied(ownerUserId).filter(_.contactUserId inSet contactUserIds).map(_.contactUserId).result

  def findOwners(contactUserId: Int) = byContactUserIdC(contactUserId).result

  def findNotDeletedIds(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(_.contactUserId).result

  def findName(ownerUserId: Int, contactUserId: Int)(implicit ec: ExecutionContext): DBIOAction[Option[String], NoStream, Effect.Read] =
    nameByPKNotDeletedC((ownerUserId, contactUserId)).result.map(_.headOption.flatten)

  def findContactIdsAll(ownerUserId: Int) =
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId).map(_.contactUserId).result

  def findContactIdsActive(ownerUserId: Int) =
    byOwnerUserIdNotDeleted(ownerUserId).map(_.contactUserId).distinct.result

  def updateName(ownerUserId: Int, contactUserId: Int, name: Option[String]): FixedSqlAction[Int, NoStream, Write] = {
    contacts.filter(c ⇒ c.ownerUserId === ownerUserId && c.contactUserId === contactUserId).map(_.localName).update(name)
  }

  def delete(ownerUserId: Int, contactUserId: Int) =
    byPKNotDeleted(ownerUserId, contactUserId).map(_.deletedAt).update(Some(LocalDateTime.now(ZoneOffset.UTC)))

  def insertOrUpdate(contact: UserContactModel) =
    contacts.insertOrUpdate(contact)
}
