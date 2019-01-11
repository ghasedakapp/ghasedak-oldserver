package im.ghasedak.server.dialog

import java.time.LocalDateTime

import im.ghasedak.api.peer.ApiPeer
import im.ghasedak.server.repo.dialog.DialogRepo
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.concurrent.ExecutionContext

object DialogUtils {

  def createOrUpdateDialog(userId: Int, peer: ApiPeer, lastMessageSeq: Int, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): DBIOAction[Unit, NoStream, Effect.Read with Effect.Write with Effect.Write] = {
    for {
      exist ← DialogRepo.usersExists(userId, peer)
      _ ← if (exist)
        DialogRepo.updateLastMessageDate(userId, peer, lastMessageSeq, lastMessageDate)
      else
        DialogRepo.create(userId, peer, lastMessageSeq, lastMessageDate)
    } yield ()
  }

}
