package ir.sndu.server

import java.net.ServerSocket

import akka.actor.ActorSystem
import com.typesafe.config.Config
import io.grpc.{ ManagedChannel, ManagedChannelBuilder }
import ir.sndu.persist.db.DbExtension
import ir.sndu.rpc.auth.AuthServiceGrpc
import ir.sndu.server.config.{ AppType, ElitemConfigFactory }
import ir.sndu.server.utils.UserTestUtils
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Inside, Matchers }

abstract class GrpcBaseSuit extends FlatSpec
  with Matchers
  with ScalaFutures
  with Inside
  with UserTestUtils {

  def randomPort: Int = {
    val socket = new ServerSocket(0)
    try {
      socket.setReuseAddress(true)
      socket.getLocalPort
    } finally {
      socket.close()
    }
  }

  protected val randomGrpcPort: Int = randomPort

  protected val config: Config = ElitemConfigFactory.load(AppType.Test)

  protected val system: ActorSystem = ElitemServerBuilder.start(config)

  protected val db = DbExtension(system).db

  protected val channel: ManagedChannel =
    ManagedChannelBuilder.forAddress("127.0.0.1", 6060).usePlaintext.build

  protected val authStub: AuthServiceGrpc.AuthServiceBlockingStub =
    AuthServiceGrpc.blockingStub(channel)

}
