package ir.sndu.server

import scala.concurrent.ExecutionContext

object Main extends App {
  val server = new HelloWorldServer(ExecutionContext.global)
  server.start()
  server.blockUntilShutdown()
  private val port = 50051
}
