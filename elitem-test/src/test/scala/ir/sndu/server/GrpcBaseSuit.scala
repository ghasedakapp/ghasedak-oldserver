package ir.sndu.server

import java.io.File

import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.server.auth.AuthServiceGrpc
import ir.sndu.server.messaging.MessagingServiceGrpc
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }

class GrpcBaseSuit extends FlatSpec
  with Matchers
  with ScalaFutures {

  private val configPath = new File(".").getCanonicalPath + "/conf/test.conf"
  System.setProperty("config.file", configPath.toString)
  System.setProperty("file.encoding", "UTF-8")
  ElitemServer.start()

  private val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress("127.0.0.1", 6060).usePlaintext(true).build

  protected implicit val authStub = AuthServiceGrpc.blockingStub(channel)
  protected implicit val messagingStub = MessagingServiceGrpc.blockingStub(channel)

}
