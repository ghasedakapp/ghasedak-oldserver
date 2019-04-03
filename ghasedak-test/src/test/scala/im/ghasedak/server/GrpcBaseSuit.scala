package im.ghasedak.server

import java.net.ServerSocket

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.grpc.scaladsl.SingleResponseRequestBuilder
import akka.stream.ActorMaterializer
import com.typesafe.config._
import im.ghasedak.rpc.auth._
import im.ghasedak.rpc.chat.{ ChatServiceClient, ChatServiceClientPowerApi }
import im.ghasedak.rpc.contact._
import im.ghasedak.rpc.messaging._
import im.ghasedak.rpc.test._
import im.ghasedak.rpc.update._
import im.ghasedak.rpc.user._
import im.ghasedak.server.config._
import im.ghasedak.server.db.DbExtension
import im.ghasedak.server.model.org.ApiKey
import im.ghasedak.server.utils._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

// todo: config this for parallel execution
abstract class GrpcBaseSuit extends FlatSpec
  with Matchers
  with ScalaFutures
  with Inside
  with UserTestUtils
  with UpdateMatcher
  with BeforeAndAfterAll {

  private def randomPort: Int = {
    val socket = new ServerSocket(0)
    try {
      socket.setReuseAddress(true)
      socket.getLocalPort
    } finally {
      socket.close()
    }
  }

  private def createConfig: Config = {
    ConfigFactory.empty().withFallback(ConfigFactory.parseString(
      s"""
         |endpoints: [
         |  {
         |    type: grpc
         |    interface: 0.0.0.0
         |    port: $randomGrpcPort
         |  }
         |]
         |akka.grpc.client {
         |  ghasedak {
         |    host = 127.0.0.1
         |    port = $randomGrpcPort
         |    use-tls = false
         |  }
         |}
         |akka.remote.netty.tcp.port: $randomAkkaPort
      """.stripMargin))
      .withFallback(GhasedakConfigFactory.load(AppType.Test))
  }

  protected type TestUser = UserTestUtils.TestClientData

  protected val randomAkkaPort: Int = randomPort

  protected val randomGrpcPort: Int = randomPort

  protected val config: Config = createConfig

  protected val officialApiKeys: Seq[ApiKey] =
    config.getConfigList("module.auth.official-api-keys")
      .asScala.map { conf ⇒
        ApiKey(
          conf.getInt("org-id"),
          conf.getString("api-key"))
      }

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5 seconds)

  protected implicit val system: ActorSystem = GhasedakServerBuilder.start(config)

  protected implicit val mat = ActorMaterializer()

  protected implicit val ec = system.dispatcher

  protected val db = DbExtension(system).db

  protected val testStub: TestServiceClientPowerApi = TestServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected val authStub: AuthServiceClientPowerApi = AuthServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected val messagingStub: MessagingServiceClientPowerApi = MessagingServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected val chatStub: ChatServiceClientPowerApi = ChatServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected val contactStub: ContactServiceClientPowerApi = ContactServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected val userStub: UserServiceClientPowerApi = UserServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected val updateStub: UpdateServiceClientPowerApi = UpdateServiceClient(GrpcClientSettings.fromConfig("ghasedak"))

  protected def sendMessageStub(token: String) = {
    messagingStub.sendMessage().addHeader(tokenMetadataKey, token)
  }

  protected def loadHistoryStub(token: String) = {
    messagingStub.loadHistory().addHeader(tokenMetadataKey, token)
  }

  protected def loadDialogsStub(token: String) = {
    messagingStub.loadDialogs().addHeader(tokenMetadataKey, token)
  }

  protected def createChatStub(token: String) = {
    chatStub.createChat().addHeader(tokenMetadataKey, token)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Await.result(system.terminate(), Duration.Inf)
  }

}
