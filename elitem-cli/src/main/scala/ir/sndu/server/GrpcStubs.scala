package ir.sndu.server

import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.server.auth.AuthServiceGrpc

object GrpcStubs {
  private val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress(CliConfigs.host, CliConfigs.port).usePlaintext(true).build
  val authStub = AuthServiceGrpc.blockingStub(channel)

}
