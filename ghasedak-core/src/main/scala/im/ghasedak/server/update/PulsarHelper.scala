package im.ghasedak.server.update

import com.sksamuel.pulsar4s.{ ConsumerConfig, PulsarClient, PulsarClientConfig }
import org.apache.pulsar.client.api.Schema

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

  protected lazy val consumer = pulsarClient.consumer[UpdateMapping](consumerConfig)

}
