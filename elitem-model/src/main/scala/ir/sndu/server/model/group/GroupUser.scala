package ir.sndu.server.model.group

import java.time.LocalDateTime

case class GroupUser(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: LocalDateTime, joinedAt: Option[LocalDateTime], isAdmin: Boolean)
