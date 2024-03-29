package pt.pak3nuh.kafka.ui.service.broker

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.PreviewCache
import pt.pak3nuh.kafka.ui.service.await
import pt.pak3nuh.kafka.ui.service.consumer.createConsumerProperties
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

private val logger = getSlfLogger<Broker>()

class Broker(val host: String, val port: Int, val securityCredentials: SecurityCredentials?, private val adminClient: AdminClient)
    : AutoCloseable {

    private val consumer = KafkaConsumer<ByteArray, ByteArray>(
            createConsumerProperties("$host:$port", "kafka-ui-broker-service-${Math.random().roundToInt()}", securityCredentials)
    )
    private val cache = PreviewCache(consumer)

    suspend fun listTopics(): Sequence<Topic> {
        return adminClient.listTopics().names().await().asSequence().map(::Topic)
    }

    suspend fun isAvailable(): Boolean {
        logger.debug("Verifying availability")
        return try {
            adminClient.describeCluster().clusterId().await()
            true
        } catch (ex: KafkaException) {
            logger.error("Error verifying availability", ex)
            false
        }
    }

    suspend fun preview(topic: Topic, refresh: Boolean, recordNumber: Int = 5): List<Record> {
        return if (refresh) cache.refresh(topic, recordNumber) else cache.get(topic, recordNumber)
    }

    override fun close() {
        logger.info("Closing broker")
        val duration = Duration.of(1, ChronoUnit.SECONDS)
        try {
            adminClient.close(duration)
            consumer.wakeup()
            consumer.close(duration)
            cache.clear()
        } catch (e: Exception) {
            logger.error("Error closing broker", e)
            throw e
        }
    }
}

typealias Record = Pair<ByteArray?, ByteArray?>
