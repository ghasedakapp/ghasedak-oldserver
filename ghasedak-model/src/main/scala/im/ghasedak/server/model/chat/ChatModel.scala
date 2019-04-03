package im.ghasedak.server.model.chat

import java.time.Instant

import im.ghasedak.api.chat.Chat
import im.ghasedak.server.model.TimeConversions._
case class ChatModel(
  id:                   Long,
  chatType:             Int,
  creatorUserId:        Int,
  title:                String,
  createdAt:            Instant,
  titleChangerUserId:   Int,
  titleChangedAt:       Instant,
  titleChangeRandomId:  Long,
  avatarChangerUserId:  Int,
  avatarChangedAt:      Instant,
  avatarChangeRandomId: Long,
  about:                Option[String] = None,
  nick:                 Option[String] = None) {
  def toApi: Chat = Chat(
    id = id,
    chatType = chatType,
    title = title,
    creatorUserId = creatorUserId,
    createdAt = Some(createdAt),
    about = about,
    nick = nick)
}