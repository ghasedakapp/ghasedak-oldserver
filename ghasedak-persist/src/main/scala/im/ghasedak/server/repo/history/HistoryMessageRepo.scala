package im.ghasedak.server.repo.history

import java.time.{ LocalDateTime, ZoneId }

import im.ghasedak.api.messaging.{ HistoryMessage, MessageContent }
import im.ghasedak.server.model.TimeConversions._
import im.ghasedak.server.repo.TypeMapper._
import slick.dbio.Effect.{ Read, Write }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import slick.sql.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }

final class HistoryMessageTable(tag: Tag) extends Table[HistoryMessage](tag, "history_messages") {

  def chatId = column[Long]("chat_id", O.PrimaryKey)

  def date = column[LocalDateTime]("date", O.PrimaryKey)

  def senderUserId = column[Int]("sender_user_id", O.PrimaryKey)

  def sequenceNr = column[Int]("sequence_nr", O.PrimaryKey)

  def messageContentHeader = column[Int]("message_content_header")

  def messageContentData = column[Array[Byte]]("message_content_data")

  def editedAt = column[Option[LocalDateTime]]("edited_at")

  def deletedAt = column[Option[LocalDateTime]]("deleted_at")

  def * = (chatId, date, senderUserId, sequenceNr, messageContentHeader, messageContentData, editedAt, deletedAt) <>
    (applyHistoryMessage.tupled, unapplyHistoryMessage)

  private def applyHistoryMessage: (Long, LocalDateTime, Int, Int, Int, Array[Byte], Option[LocalDateTime], Option[LocalDateTime]) ⇒ HistoryMessage = {
    case (chatId, date, senderUserId, sequenceNr, messageContentHeader, messageContentData, editedAt, deletedAt) ⇒
      HistoryMessage(
        chatId = chatId,
        sequenceNr = sequenceNr,
        date = Some(date),
        senderUserId = senderUserId,
        message = Some(MessageContent.parseFrom(messageContentData)),
        editedAt = editedAt,
        deletedAt = deletedAt)
  }

  private def unapplyHistoryMessage: HistoryMessage ⇒ Option[(Long, LocalDateTime, Int, Int, Int, Array[Byte], Option[LocalDateTime], Option[LocalDateTime])] = { historyMessage ⇒
    HistoryMessage.unapply(historyMessage) map {
      case (chatId, sequenceNr, date, senderUserId, message, _, _, _, editedAt, deletedAt, _) ⇒
        (
          chatId,
          date.get,
          senderUserId,
          sequenceNr,
          message.get.body.number,
          message.get.toByteArray,
          editedAt,
          deletedAt)
    }
  }

}

object HistoryMessageRepo {

  val SharedUserId = 0

  val messages = TableQuery[HistoryMessageTable]
  val messagesC = Compiled(messages)

  val notDeletedMessages = messages.filter(_.deletedAt.isEmpty)

  val withoutServiceMessages = notDeletedMessages.filter(_.messageContentHeader =!= 2)

  def byChatId(chatId: Rep[Long]): Query[HistoryMessageTable, HistoryMessage, Seq] =
    notDeletedMessages
      .filter(m ⇒ m.chatId === chatId)

  def create(message: HistoryMessage): FixedSqlAction[Int, NoStream, Write] =
    messagesC += message

  def create(newMessages: Seq[HistoryMessage]): FixedSqlAction[Option[Int], NoStream, Write] =
    messagesC ++= newMessages

  def find(chatId: Long, dateOpt: Option[LocalDateTime], limit: Int): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] = {
    val baseQuery = notDeletedMessages
      .filter(m ⇒
        m.chatId === chatId)

    val query = dateOpt match {
      case Some(date) ⇒
        baseQuery.filter(_.date <= date).sortBy(_.date.desc)
      case None ⇒
        baseQuery.sortBy(_.date.asc)
    }

    query.take(limit).result
  }

  private val afterC = Compiled { (chatId: Rep[Long], seq: Rep[Int], limit: ConstColumn[Long]) ⇒
    byChatId(chatId)
      .filter(_.sequenceNr >= seq)
      .sortBy(_.sequenceNr.asc)
      .take(limit)
  }

  def findAfter(chatId: Long, seq: Int, limit: Long): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    afterC((chatId, seq, limit)).result

  private val metaAfterC = Compiled { (chatId: Rep[Long], date: Rep[LocalDateTime], limit: ConstColumn[Long]) ⇒
    byChatId(chatId)
      .filter(_.date > date)
      .sortBy(_.date.asc)
      .take(limit)
      .map(hm ⇒ (hm.sequenceNr, hm.date, hm.senderUserId, hm.messageContentHeader))
  }

  def findMetaAfter(chatId: Long, date: LocalDateTime, limit: Long): FixedSqlStreamingAction[Seq[(Int, LocalDateTime, Int, Int)], (Int, LocalDateTime, Int, Int), Read] =
    metaAfterC((chatId, date, limit)).result

  private val beforeC = Compiled { (chatId: Rep[Long], seq: Rep[Int], limit: ConstColumn[Long]) ⇒
    byChatId(chatId)
      .filter(_.sequenceNr <= seq)
      .sortBy(_.sequenceNr.desc)
      .take(limit)
  }

  private val beforeExclC = Compiled { (chatId: Rep[Long], seq: Rep[Int], limit: ConstColumn[Long]) ⇒
    byChatId(chatId)
      .filter(_.sequenceNr < seq)
      .sortBy(_.date.asc)
      .take(limit)
  }

  def findBefore(chatId: Long, seq: Int, limit: Long): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    beforeC((chatId, seq, limit)).result

  def findBydi(chatId: Long, seq: Int, limit: Long): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    (beforeExclC.applied((chatId, seq, limit)) ++
      afterC.applied((chatId, seq, limit))).result

  def findBySender(senderUserId: Int, chatId: Long, sequenceNr: Int): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    notDeletedMessages.filter(m ⇒ m.senderUserId === senderUserId && m.chatId === chatId && m.sequenceNr === sequenceNr).result

  def findNewest(chatId: Long): SqlAction[Option[HistoryMessage], NoStream, Read] =
    notDeletedMessages
      .filter(m ⇒ m.chatId === chatId)
      .sortBy(_.date.desc)
      .take(1)
      .result
      .headOption

  def find(chatId: Long): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    notDeletedMessages
      .filter(m ⇒ m.chatId === chatId)
      .sortBy(_.date.desc)
      .result

  def find(chatId: Long, sequenceNumbers: Set[Int]): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    notDeletedMessages.filter(m ⇒ m.chatId === chatId && (m.sequenceNr inSet sequenceNumbers)).result

  def getUnreadCount(chatId: Long, clientUserId: Int, lastReadAt: LocalDateTime, noServiceMessages: Boolean = false): FixedSqlAction[Int, NoStream, Read] =
    (if (noServiceMessages) withoutServiceMessages else notDeletedMessages)
      .filter(m ⇒ m.chatId === chatId)
      .filter(m ⇒ m.date > lastReadAt && m.senderUserId =!= clientUserId)
      .length
      .result

  def deleteAll(chatId: Long): FixedSqlAction[Int, NoStream, Write] = {
    notDeletedMessages
      .filter(m ⇒ m.chatId === chatId)
      .map(_.deletedAt)
      .update(Some(LocalDateTime.now(ZoneId.systemDefault())))
  }

  def delete(chatId: Long, sequenceNumbers: Set[Int]): FixedSqlAction[Int, NoStream, Write] =
    notDeletedMessages
      .filter(m ⇒ m.chatId === chatId)
      .filter(_.sequenceNr inSet sequenceNumbers)
      .map(_.deletedAt)
      .update(Some(LocalDateTime.now(ZoneId.systemDefault())))
}
