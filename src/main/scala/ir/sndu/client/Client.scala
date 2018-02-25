package ir.sndu.client

import io.grpc.ManagedChannelBuilder

object Client extends App {
  val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).usePlaintext(true).build
  //  val request = HelloRequest(name = "World")
  //
  //  val blockingStub = GreeterGrpc.blockingStub(channel)
  //  val reply: HelloReply = blockingStub.sayHello(request)
  //  println(reply)

}
