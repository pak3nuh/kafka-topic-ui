package pt.pak3nuh.kafka.ui.service.broker

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.consumer.createConsumerProperties
import pt.pak3nuh.kafka.ui.service.deserializer.Deserializer
import java.time.Duration
import kotlin.coroutines.coroutineContext

private val logger = getSlfLogger<Subscription>()

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
        groupId: String,
        securityCredentials: SecurityCredentials?
) : AutoCloseable {
    private val consumer = KafkaConsumer<ByteArray, ByteArray>(
            createConsumerProperties("$host:$port", groupId, securityCredentials, earliest = false)
    )

    fun initSync() {
        logger.info("Subscribing topic {}", topic)
        consumer.subscribe(listOf(topic.name))
    }

    fun pollSync(timeout: Duration): Sequence<Pair<String?, String?>> {
        if (logger.isDebugEnabled) {
            data class Assignment(val topicPartition: TopicPartition, val position: Long)
            val assignment = consumer.assignment().map {
                Assignment(it, consumer.position(it))
            }
            logger.debug("Polling records on assignment {}", assignment)
        }
        val records: ConsumerRecords<ByteArray?, ByteArray?> = consumer.poll(timeout)
        logger.trace("Returned records {}", records)
        return records.asSequence()
                .map {
                    val key = it.key()?.let(keyDeserializer::deserialize)
                    val value = it.value()?.let(valueDeserializer::deserialize)
                    Pair(key, value)
                }
    }

    tailrec suspend fun seekWhenReady(offset: Long) {
        if (!coroutineContext.isActive) {
            return
        }
        consumer.poll(Duration.ZERO)
        val assignment = consumer.assignment()
        if (assignment.isEmpty()) {
            logger.debug("Assignment not ready. Trying in 200ms")
            delay(200)
            seekWhenReady(offset)
        } else {
            logger.debug("Assignment is {}", assignment)
            assignment.forEach {
                logger.trace("Seeking {} to offset {}", it, offset)
                consumer.seek(it, offset)
            }
        }
    }

    override fun close() {
        consumer.unsubscribe()
        consumer.close(Duration.ofSeconds(1))
    }
}