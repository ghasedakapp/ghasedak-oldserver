package im.ghasedak.server.update

import akka.NotUsed
import akka.stream.scaladsl.{ BroadcastHub, Keep, RunnableGraph, Source, StreamRefs }
import akka.stream.{ ActorMaterializer, KillSwitches, SourceRef, UniqueKillSwitch }
import com.sksamuel.pulsar4s.akka.streams.Control
import com.sksamuel.pulsar4s.{ Consumer, ConsumerConfig, PulsarClient, PulsarClientConfig }
import im.ghasedak.rpc.update.ResponseGetDifference
import org.apache.pulsar.client.api.Schema

import scala.concurrent.Future
import scala.util.Try

trait PulsarHelper {
  this: UpdateProcessor ⇒

  case class SourceSwitch(control: Control, source: Source[ResponseGetDifference, NotUsed])

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
  protected var updateSource: Option[SourceSwitch] = None

  protected def createConsumer: Consumer[UpdateMapping] = {
    Try(consumer.foreach(_.close()))
    consumer = Some(pulsarClient.consumer[UpdateMapping](consumerConfig))
    consumer.get
  }

  protected def createSource(): SourceSwitch = {
    val matValues = com.sksamuel.pulsar4s.akka.streams.committableSource(() ⇒ createConsumer, None)
      .map(i ⇒ buildDifference(tokenId, i.message))
      .toMat(BroadcastHub.sink(bufferSize = 64))(Keep.both)
      .run()
    updateSource = Some(SourceSwitch(matValues._1, matValues._2))
    updateSource.get
  }

  protected def createSourceRef: Option[Future[SourceRef[ResponseGetDifference]]] = {
    //TODO Removes old ref. Only keep one active sourceref
    updateSource.map(_.source.runWith(StreamRefs.sourceRef[ResponseGetDifference]())(mat))
  }

}
