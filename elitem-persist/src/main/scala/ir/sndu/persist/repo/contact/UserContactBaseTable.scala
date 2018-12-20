package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import slick.lifted.Tag

abstract class UserContactBaseTable[T](tag: Tag, tname: String) extends Table[T](tag, tname) {

  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)

  def contactUserId = column[Int]("contact_user_id", O.PrimaryKey)

  def name = column[Option[String]]("name")

  def isDeleted = column[Boolean]("is_deleted", O.Default(false))

}
