package im.ghasedak.server.dialog

import java.time.LocalDateTime

import im.ghasedak.server.repo.dialog.DialogRepo
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.concurrent.ExecutionContext

object DialogUtils {

  //  def createOrUpdateDialog(userId:Int, chatId: Long, lastMessageSeq: Int, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): DBIOAction[Unit, NoStream, Effect.Read with Effect.Write with Effect.Write] = {
  //    for {
  //      _ ← DialogRepo.updateLastMessageSeqDate(chatId, lastMessageSeq, lastMessageDate)
  //    } yield ()
  //  }

}
