package ir.sndu.persist.repo.org

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.server.model.org.Organization
import slick.lifted.Tag

final class OrganizationTable(tag: Tag) extends Table[Organization](tag, "organizations") {

  def id = column[Int]("id", O.PrimaryKey)

  def name = column[String]("name")

  override def * = (id, name) <> (Organization.tupled, Organization.unapply)

}

object OrganizationRepo {

}
