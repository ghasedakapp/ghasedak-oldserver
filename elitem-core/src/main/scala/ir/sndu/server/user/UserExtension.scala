package ir.sndu.server.user

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }

class UserExtensionImpl(system: ExtendedActorSystem) extends Extension {
  private implicit val _system: ActorSystem = system
  private val region = UserProcessorRegion.start()

  def send(): Unit = {
    println("wa sent")
    region ! UserEnvelope(10).withSendMessage(UserCommands.SendMessage(10, Some(Peer(10, PeerType.PRIVATE))))
  }
}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserExtensionImpl = new UserExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserExtension
}
