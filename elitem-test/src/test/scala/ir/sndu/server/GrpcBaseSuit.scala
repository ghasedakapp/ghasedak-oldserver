package ir.sndu.server

import com.typesafe.config.Config
import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.server.config.{ AppType, ElitemConfigFactory }
import ir.sndu.server.rpcauth.AuthServiceGrpc
import ir.sndu.server.rpccontacts.ContactServiceGrpc
import ir.sndu.server.rpcgroups.GroupServiceGrpc
import ir.sndu.server.rpcmessaging.MessagingServiceGrpc
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Inside, Matchers }

class GrpcBaseSuit extends FlatSpec
  with Matchers
  with ScalaFutures
  with Inside {

  protected val config: Config = ElitemConfigFactory.load(AppType.Test)

  ElitemServerBuilder.start(config)

  private val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress("127.0.0.1", 6060).usePlaintext(true).build

  protected implicit val authStub: AuthServiceGrpc.AuthServiceBlockingStub =
    AuthServiceGrpc.blockingStub(channel)
  protected implicit val messagingStub: MessagingServiceGrpc.MessagingServiceBlockingStub =
    MessagingServiceGrpc.blockingStub(channel)
  protected implicit val contactStub: ContactServiceGrpc.ContactServiceBlockingStub =
    ContactServiceGrpc.blockingStub(channel)
  protected implicit val groupStub: GroupServiceGrpc.GroupServiceBlockingStub =
    GroupServiceGrpc.blockingStub(channel)

}
