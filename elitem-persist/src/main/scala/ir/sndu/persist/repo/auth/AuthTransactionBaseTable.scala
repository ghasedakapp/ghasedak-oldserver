package ir.sndu.persist.repo.auth

import java.time.LocalDateTime

import com.github.tminglei.slickpg.ExPostgresProfile.api._
import ir.sndu.persist.repo.TypeMapper._
import slick.lifted.Tag

abstract class AuthTransactionBaseTable[T](tag: Tag, tname: String) extends Table[T](tag, tname) {

  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)

  def appId = column[Int]("app_id")

  def apiKey = column[String]("api_key")

  def deviceHash = column[String]("device_hash")

  def deviceInfo = column[String]("device_info")

  def createdAt = column[LocalDateTime]("created_at")

  def isChecked = column[Boolean]("is_checked")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

}
