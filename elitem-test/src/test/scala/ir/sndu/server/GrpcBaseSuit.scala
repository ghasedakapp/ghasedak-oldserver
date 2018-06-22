package ir.sndu.server

import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.server.auth.AuthServiceGrpc
import ir.sndu.server.config.{ AppType, ElitemConfigFactory }
import ir.sndu.server.contacts.ContactServiceGrpc
import ir.sndu.server.messaging.MessagingServiceGrpc
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }

class GrpcBaseSuit extends FlatSpec
  with Matchers
  with ScalaFutures {

  protected val conf = ElitemConfigFactory.load(AppType.Test)

  ElitemServer.start()

  private val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress("127.0.0.1", 6060).usePlaintext(true).build

  protected implicit val authStub = AuthServiceGrpc.blockingStub(channel)
  protected implicit val messagingStub = MessagingServiceGrpc.blockingStub(channel)
  protected implicit val contactStub = ContactServiceGrpc.blockingStub(channel)

}
