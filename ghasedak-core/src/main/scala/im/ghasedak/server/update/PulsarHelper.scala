package im.ghasedak.server.update

import akka.stream.scaladsl.{ Keep, Source, StreamRefs }
import akka.stream.{ ActorMaterializer, KillSwitches, SourceRef, UniqueKillSwitch }
import com.sksamuel.pulsar4s.{ Consumer, ConsumerConfig, PulsarClient, PulsarClientConfig }
import im.ghasedak.rpc.update.ResponseGetDifference
import org.apache.pulsar.client.api.Schema

import scala.concurrent.Future
import scala.util.Try

trait PulsarHelper {
  this: UpdateProcessor ⇒

  case class SourceRefSwitch(killSwitch: UniqueKillSwitch, sourceRefFuture: Future[SourceRef[ResponseGetDifference]])

  protected lazy val pulsarHost: String = config.getString("module.update.pulsar.host")
  protected lazy val pulsarPort: Int = config.getInt("module.update.pulsar.port")
  protected lazy val pulsarClientConfig: PulsarClientConfig = PulsarClientConfig(s"pulsar://$pulsarHost:$pulsarPort")
  protected lazy val pulsarClient = PulsarClient(pulsarClientConfig)

  protected implicit val updateMappingSchema: Schema[UpdateMapping] = UpdateMappingSchema()

  protected lazy val topic = getUserUpdateTopic(userId)
  protected lazy val consumerConfig = ConsumerConfig(
    subscriptionName = getSubscription(userId, tokenId),
    topics = Seq(topic))

  protected var consumer: Option[Consumer[UpdateMapping]] = None
  protected var updateSourceRef: Option[SourceRefSwitch] = None

  protected def createConsumer: Consumer[UpdateMapping] = {
    Try(consumer.foreach(_.close()))
    consumer = Some(pulsarClient.consumer[UpdateMapping](consumerConfig))
    consumer.get
  }

  protected def createSource(): Source[ResponseGetDifference, UniqueKillSwitch] = {
    com.sksamuel.pulsar4s.akka.streams.committableSource(() ⇒ createConsumer, None)
      .viaMat(KillSwitches.single)(Keep.right)
      .map(i ⇒ buildDifference(tokenId, i.message))
  }

  protected def getSourceRef: SourceRefSwitch = {
    val matValue = createSource().toMat(StreamRefs.sourceRef[ResponseGetDifference]())(Keep.both).run()(mat)
    SourceRefSwitch(matValue._1, matValue._2)
  }

}
