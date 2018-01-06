package ir.sndu.server.user

trait UserCommandHandler {

  def sendMessage(peer: Peer): Unit = {
    println(peer)
  }

}
