package ir.sndu.server.model.group

import java.time.LocalDateTime

import ir.sndu.api.group.ApiGroupType

case class Group(
                  id: Int,
                  creatorUserId: Int,
                  accessHash: Long,
                  title: String,
                  createdAt: LocalDateTime,
                  typ: ApiGroupType,
                  about: Option[String],
                  topic: Option[String]
                )

object Group {
  def fromFull(fullGroup: FullGroup): Group =
    Group(
      id = fullGroup.id,
      creatorUserId = fullGroup.creatorUserId,
      accessHash = fullGroup.accessHash,
      title = fullGroup.title,
      createdAt = fullGroup.createdAt,
      typ = fullGroup.typ,
      about = fullGroup.about,
      topic = fullGroup.topic
    )
}

case class FullGroup(
                      id: Int,
                      creatorUserId: Int,
                      accessHash: Long,
                      title: String,
                      createdAt: LocalDateTime,
                      typ: ApiGroupType,
                      about: Option[String],
                      topic: Option[String],
                      titleChangerUserId: Int,
                      titleChangedAt: LocalDateTime,
                      titleChangeRandomId: Long,
                      avatarChangerUserId: Int,
                      avatarChangedAt: LocalDateTime,
                      avatarChangeRandomId: Long,
                    )
