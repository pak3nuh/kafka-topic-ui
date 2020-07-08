package pt.pak3nuh.kafka.ui.service.broker

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import pt.pak3nuh.kafka.ui.app.applicationUUID
import pt.pak3nuh.kafka.ui.service.consumer.createConsumerProperties
import pt.pak3nuh.kafka.ui.service.deserializer.Deserializer
import java.time.Duration

/**
 * Topic specific subscription.
 *
 * Shares the group id with the other subscriptions for the same topic with auto-commit semantics.
 */
class Subscription(
        private val keyDeserializer: Deserializer,
        private val valueDeserializer: Deserializer,
        private val topic: Topic,
        host: String,
        port: Int,
        earliest: Boolean
) : AutoCloseable {
    private val consumer = KafkaConsumer<ByteArray, ByteArray>(
            createConsumerProperties("$host:$port", "kafka-ui-topic-listener-${applicationUUID}-${topic.name}", earliest = earliest)
    )

    fun initSync() {
        // todo if same topic is opened several times, it doesn't start from the beginning
        consumer.subscribe(listOf(topic.name))
    }

    fun pollSync(timeout: Duration): Sequence<Pair<String?, String?>> {
        val records: ConsumerRecords<ByteArray?, ByteArray?> = consumer.poll(timeout)
        return records.asSequence()
                .map {
                    val key = it.key()?.let(keyDeserializer::deserialize)
                    val value = it.value()?.let(valueDeserializer::deserialize)
                    Pair(key, value)
                }
    }

    override fun close() {
        consumer.unsubscribe()
        consumer.close(Duration.ofSeconds(1))
    }
}