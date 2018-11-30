package ir.sndu.server.user

import java.time.LocalDateTime

import ir.sndu.persist.repo.dialog.DialogRepo
import ir.sndu.server.apipeer.ApiPeer
import slick.dbio.{ DBIOAction, Effect, NoStream }

import scala.concurrent.ExecutionContext

object DialogUtils {

  def createOrUpdateDialog(userId: Int, peer: ApiPeer, lastMessageDate: LocalDateTime)(implicit ec: ExecutionContext): DBIOAction[Unit, NoStream, Effect.Read with Effect.Write with Effect.Write] = {
    for {
      exist ← DialogRepo.usersExists(userId, peer)
      _ ← if (exist)
        DialogRepo.updateLastMessageDate(userId, peer, lastMessageDate)
      else
        DialogRepo.create(userId, peer, lastMessageDate)

    } yield ()
  }
}
