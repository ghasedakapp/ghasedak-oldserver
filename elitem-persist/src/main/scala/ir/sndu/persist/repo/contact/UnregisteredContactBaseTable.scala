package ir.sndu.persist.repo.contact

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import slick.lifted.Tag

abstract class UnregisteredContactBaseTable[T](tag: Tag, tname: String) extends Table[T](tag, tname) {

  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)

  def localName = column[String]("local_name")

}
