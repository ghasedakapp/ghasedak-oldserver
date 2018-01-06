package ir.sndu.server

import io.grpc.{ Server, ServerBuilder }

import scala.concurrent.{ ExecutionContext, Future }
import ir.sndu.server.hello._

object HelloWorldServer {
  val port = 6060
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  def start(): Unit = {
    server = ServerBuilder.forPort(HelloWorldServer.port).addService(GreeterGrpc.bindService(new GreeterImpl, executionContext)).build.start
    //    ir.sndu.server.HelloWorldServer.logger.info("Server started, listening on " + ir.sndu.server.HelloWorldServer.port)
    println("Server started, listening on " + HelloWorldServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def sayHello(req: HelloRequest) = {
      val reply = HelloReply(message = "Hello " + req.name)
      println(req)
      Future.successful(reply)
    }
  }

}
