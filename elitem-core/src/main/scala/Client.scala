import io.grpc.ManagedChannelBuilder

import scala.concurrent.Future
import ir.sndu.server.hello._

object Client extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).usePlaintext(true).build
  val request = HelloRequest(name = "World")

  val blockingStub = GreeterGrpc.blockingStub(channel)
  val reply: HelloReply = blockingStub.sayHello(request)
  println(reply)

}
