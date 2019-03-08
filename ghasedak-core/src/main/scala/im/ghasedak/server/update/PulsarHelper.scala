package im.ghasedak.server.update

import com.sksamuel.pulsar4s.{ Consumer, ConsumerConfig, PulsarClient, PulsarClientConfig }
import org.apache.pulsar.client.api.Schema

import scala.util.Try

trait PulsarHelper {
  this: UpdateProcessor ⇒

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

  protected def createConsumer: Consumer[UpdateMapping] = {
    Try(consumer.foreach(_.close()))
    consumer = Some(pulsarClient.consumer[UpdateMapping](consumerConfig))
    consumer.get
  }
}
