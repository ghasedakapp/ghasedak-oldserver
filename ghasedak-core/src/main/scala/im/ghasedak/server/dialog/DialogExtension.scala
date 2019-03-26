package im.ghasedak.server.dialog

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import im.ghasedak.rpc.messaging.ResponseLoadDialogs
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.model.dialog.DialogModel
import im.ghasedak.server.repo.dialog.DialogRepo
import im.ghasedak.server.repo.history.HistoryMessageRepo
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile

import scala.concurrent.{ ExecutionContext, Future }

final class DialogExtensionImpl(system: ExtendedActorSystem) extends Extension {

  private implicit val _system: ActorSystem = system

  // todo: use separate dispatcher for dialog extension jobs and chat processors actor
  private implicit val ec: ExecutionContext = system.dispatcher

  private implicit val db: PostgresProfile.backend.Database = DbExtension(system).db

  def loadDialogs(userId: Int, limit: Int): Future[ResponseLoadDialogs] = {
    val action = for {
      dialogs ← DialogRepo.find(userId, limit)
      fullDialogs ← DBIO.sequence(dialogs.map(getDialog))
    } yield ResponseLoadDialogs(fullDialogs)
    db.run(action)
  }

  private def getDialog(dialog: DialogModel) =
    for {
      apiDialog ← HistoryMessageRepo.find(dialog.chatId, Some(dialog.lastMessageDate), 1).headOption.map(dialog.toApi)
      firstUnreadOpt ← HistoryMessageRepo.findAfter(dialog.chatId, dialog.lastReadSeq, 1) map (_.headOption)
    } yield {
      apiDialog.copy(firstUnreadSeq = firstUnreadOpt.map(_.sequenceNr))
    }
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): DialogExtensionImpl = new DialogExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = DialogExtension
}

