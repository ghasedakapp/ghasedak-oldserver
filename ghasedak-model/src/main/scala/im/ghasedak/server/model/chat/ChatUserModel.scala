package im.ghasedak.server.model.chat

import java.time.{ Instant, LocalDateTime }

case class ChatUserModel(
  chatId:        Long,
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     LocalDateTime,
  joinedAt:      Option[LocalDateTime],
  isAdmin:       Boolean)
