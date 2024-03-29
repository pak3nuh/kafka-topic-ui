package pt.pak3nuh.kafka.ui.service.broker

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.config.SettingsConfig
import pt.pak3nuh.kafka.ui.service.consumer.createProducerProperties
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerProviderService
import pt.pak3nuh.kafka.ui.view.coroutine.onKafka
import java.time.Duration

@Lazy
@Service
class SubscriptionService @Autowired constructor(
        private val broker: Broker,
        private val deserializerProviderService: DeserializerProviderService,
        private val settingsConfig: SettingsConfig
) : AutoCloseable {

    private val closeHandlers = mutableListOf<() -> Unit>()
    private val producer: KafkaProducer<ByteArray, ByteArray> by lazy { buildProducer() }

    private fun buildProducer(): KafkaProducer<ByteArray, ByteArray> {
        val kafkaProducer = KafkaProducer<ByteArray, ByteArray>(createProducerProperties("${broker.host}:${broker.port}", broker.securityCredentials))
        closeHandlers.add { producer.close(Duration.ofSeconds(1)) }
        return kafkaProducer
    }

    fun subscribe(keyDeserializer: DeserializerMetadata, valueDeserializer: DeserializerMetadata, topic: Topic): Subscription {
        val key = deserializerProviderService.createDeserializer(keyDeserializer)
        val value = deserializerProviderService.createDeserializer(valueDeserializer)
        val groupId = "kafka-ui-topic-listener-${settingsConfig.applicationUUID}-${topic.name}"
        val subscription = Subscription(key, value, topic, broker.host, broker.port, groupId, broker.securityCredentials)
        subscription.initSync()
        return subscription
    }

    suspend fun sendRecord(topic: Topic, key: ByteArray? = null, value: ByteArray? = null) {
        onKafka {
            val future = producer.send(ProducerRecord(topic.name, key, value))
            future.get() // blocking call on blocking dispatcher
        }
    }

    override fun close() {
        closeHandlers.forEach { it() }
    }
}