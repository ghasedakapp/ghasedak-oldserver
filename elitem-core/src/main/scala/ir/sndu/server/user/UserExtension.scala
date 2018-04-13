package ir.sndu.server.user

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.pattern.ask
import akka.util.Timeout
import ir.sndu.server.messaging.ApiMessage
import ir.sndu.server.peer.ApiPeer
import ir.sndu.server.user.UserCommands.SendMessageAck

import scala.concurrent.duration._
import scala.concurrent.Future
class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {
  implicit private val _system: ActorSystem = system
  implicit private val timeout: Timeout = Timeout(30.seconds)

  private val region = UserProcessorRegion.start()

  def sendMessage(userId: Int, peer: ApiPeer, randomId: Long, message: ApiMessage): Future[SendMessageAck] =
    (region ? UserEnvelope(userId).withSendMessage(
      UserCommands.SendMessage(userId, Some(peer), randomId))).mapTo[SendMessageAck]

}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserExtensionImpl = new UserExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserExtension
}
