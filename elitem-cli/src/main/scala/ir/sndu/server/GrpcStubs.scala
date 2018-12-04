package ir.sndu.server

import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.rpc.auth.AuthServiceGrpc
import ir.sndu.rpc.contact.ContactServiceGrpc
import ir.sndu.rpc.messaging.MessagingServiceGrpc
import ir.sndu.rpc.user.UserServiceGrpc

object GrpcStubs {
  private val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress(CliConfigs.host, CliConfigs.port).usePlaintext(true).build
  val authStub: AuthServiceGrpc.AuthServiceBlockingStub = AuthServiceGrpc.blockingStub(channel)
  val messagingStub: MessagingServiceGrpc.MessagingServiceBlockingStub = MessagingServiceGrpc.blockingStub(channel)
  val contactsStub: ContactServiceGrpc.ContactServiceBlockingStub = ContactServiceGrpc.blockingStub(channel)
  val userStub: UserServiceGrpc.UserServiceBlockingStub = UserServiceGrpc.blockingStub(channel)
}
