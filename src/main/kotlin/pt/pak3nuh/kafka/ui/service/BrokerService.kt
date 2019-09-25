package pt.pak3nuh.kafka.ui.service

import kotlinx.coroutines.withContext
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.utils.Bytes
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.app.wrapEx
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.consumer.createConsumerProperties
import pt.pak3nuh.kafka.ui.view.coroutine.KafkaDispatcher
import java.time.Duration
import kotlin.math.roundToInt

@Service
class BrokerService {

    fun connect(
            host: String,
            port: Int,
            timeoutMs: Int = 5_000
    ): Broker {
        return wrapEx {
            val adminClient = AdminClient.create(mapOf(
                    "bootstrap.servers" to "$host:$port",
                    "group.id" to "kafka-topic-ui-app",
                    "request.timeout.ms" to timeoutMs
            ))
            Broker(host, port, adminClient)
        }
    }

}

private val logger = getSlfLogger<Broker>()

class Broker(val host: String, val port: Int, private val adminClient: AdminClient) : AutoCloseable {

    private val consumer = KafkaConsumer<Bytes, ByteArray>(
            createConsumerProperties("$host:$port", "kafka-ui-broker-service-${Math.random().roundToInt()}")
    )

    suspend fun listTopics(): Sequence<Topic> {
        return adminClient.listTopics().names().await().map(::Topic).asSequence()
    }

    suspend fun isAvailable(): Boolean {
        logger.debug("Verifying availability")
        return try{
            adminClient.describeCluster().clusterId().await()
            true
        } catch (ex: KafkaException) {
            logger.error("Error verifying availability", ex)
            false
        }
    }

    suspend fun preview(topic: Topic, recordNumber: Int = 5): List<Record> {
        return withContext(KafkaDispatcher) {
            consumer.subscribe(listOf(topic.name))
            val records = consumer.poll(Duration.ofSeconds(1))
            val result = records.map {
                Pair(it.key().get(), it.value())
            }.take(recordNumber)
            consumer.unsubscribe()
            result
        }
    }

    override fun close() {
        adminClient.close()
        consumer.close()
    }
}

class Topic(val name: String) {
    override fun toString(): String {
        return name
    }
}

typealias Record = Pair<ByteArray?, ByteArray?>