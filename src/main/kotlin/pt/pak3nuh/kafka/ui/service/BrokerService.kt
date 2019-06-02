package pt.pak3nuh.kafka.ui.service

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.common.KafkaException
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.app.wrapEx

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
                    "group.id" to "test",
                    "request.timeout.ms" to timeoutMs
            ))
            Broker(host, port, adminClient)
        }
    }

}

class Broker(private val host: String, private val port: Int, private val adminClient: AdminClient) {
    suspend fun listTopics(): Sequence<Topic> {
        return adminClient.listTopics().names().thenApply { it.map(::Topic).asSequence() }.await()
    }

    suspend fun isAvailable(): Boolean {
        return try{
            adminClient.describeCluster().clusterId().await()
            true
        } catch (ex: KafkaException) {
            false
        }
    }
}

data class Topic(val name: String)