package ir.sndu.persist.repo.group

import java.time.LocalDateTime

import ir.sndu.persist.repo.TypeMapper._
import ir.sndu.persist.repo.group.GroupTypeColumnType._
import ir.sndu.server.groups.ApiGroupType
import ir.sndu.server.model.group.{FullGroup, Group}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

final class FullGroupTable(tag: Tag) extends Table[FullGroup](tag, "groups") {
  def id = column[Int]("id", O.PrimaryKey)

  def creatorUserId = column[Int]("creator_user_id")

  def accessHash = column[Long]("access_hash")

  def title = column[String]("title")

  def createdAt = column[LocalDateTime]("created_at")

  def `type` = column[ApiGroupType]("type")

  def about = column[Option[String]]("about")

  def topic = column[Option[String]]("topic")

  def titleChangerUserId = column[Int]("title_changer_user_id")

  def titleChangedAt = column[LocalDateTime]("title_changed_at")

  def titleChangeRandomId = column[Long]("title_change_random_id")

  def avatarChangerUserId = column[Int]("avatar_changer_user_id")

  def avatarChangedAt = column[LocalDateTime]("avatar_changed_at")

  def avatarChangeRandomId = column[Long]("avatar_change_random_id")

  def * =
    (
      id,
      creatorUserId,
      accessHash,
      title,
      createdAt,
      `type`,
      about,
      topic,
      titleChangerUserId,
      titleChangedAt,
      titleChangeRandomId,
      avatarChangerUserId,
      avatarChangedAt,
      avatarChangeRandomId,
    ) <> (FullGroup.tupled, FullGroup.unapply)

  def asGroup = (id, creatorUserId, accessHash, title, createdAt,`type`, about, topic) <> ((Group.apply _).tupled, Group.unapply)
}

object GroupRepo {
  val groups = TableQuery[FullGroupTable]
  val groupsC = Compiled(groups)

  def byId(id: Rep[Int]) = groups filter (_.id === id)
  def groupById(id: Rep[Int]) = byId(id) map (_.asGroup)
  def titleById(id: Rep[Int]) = byId(id) map (_.title)

  val byIdC = Compiled(byId _)
  val groupByIdC = Compiled(groupById _)
  val titleByIdC = Compiled(titleById _)

  val allIds = groups.map(_.id)

  def create(group: Group, randomId: Long) = {
    groups += FullGroup(
      id = group.id,
      creatorUserId = group.creatorUserId,
      accessHash = group.accessHash,
      title = group.title,
      createdAt = group.createdAt,
      typ = group.typ,
      about = group.about,
      topic = group.topic,
      titleChangerUserId = group.creatorUserId,
      titleChangedAt = group.createdAt,
      titleChangeRandomId = randomId,
      avatarChangerUserId = group.creatorUserId,
      avatarChangedAt = group.createdAt,
      avatarChangeRandomId = randomId
    )
  }

  // TODO: Replace with key value
  def findAllIds = allIds.result

  def find(id: Int) =
    groupByIdC(id).result.headOption

  def findFull(id: Int) =
    byIdC(id).result.headOption

  def updateTitle(id: Int, title: String, changerUserId: Int, randomId: Long, date: LocalDateTime) =
    byIdC.applied(id)
      .map(g ⇒ (g.title, g.titleChangerUserId, g.titleChangedAt, g.titleChangeRandomId))
      .update((title, changerUserId, date, randomId))

  def updateTopic(id: Int, topic: Option[String]) =
    byIdC.applied(id).map(_.topic).update(topic)

  def updateAbout(id: Int, about: Option[String]) =
    byIdC.applied(id).map(_.about).update(about)

}
