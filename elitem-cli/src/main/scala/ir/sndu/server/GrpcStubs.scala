package ir.sndu.server

import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.server.rpcauth.AuthServiceGrpc
import ir.sndu.server.rpccontact.ContactServiceGrpc
import ir.sndu.server.rpcmessaging.MessagingServiceGrpc
import ir.sndu.server.rpcuser.UserServiceGrpc

object GrpcStubs {
  private val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress(CliConfigs.host, CliConfigs.port).usePlaintext(true).build
  val authStub = AuthServiceGrpc.blockingStub(channel)
  val messagingStub = MessagingServiceGrpc.blockingStub(channel)
  val contactsStub = ContactServiceGrpc.blockingStub(channel)
  val userStub = UserServiceGrpc.blockingStub(channel)
}
