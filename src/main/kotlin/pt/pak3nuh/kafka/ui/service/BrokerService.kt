package pt.pak3nuh.kafka.ui.service

import org.apache.kafka.clients.admin.AdminClient
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.app.wrapEx

@Service
class BrokerService {

    fun connect(host: String, port: Int): Broker {
        return wrapEx {
            val adminClient = AdminClient.create(mapOf(
                    "bootstrap.servers" to "$host:$port",
                    "group.id" to "test"
            ))
            Broker(host, port, adminClient)
        }
    }

}

class Broker(private val host: String, private val port: Int, private val adminClient: AdminClient) {
    suspend fun listTopics(): Sequence<Topic> {
        return adminClient.listTopics().names().thenApply { it.map(::Topic).asSequence() }.await()
    }
}

data class Topic(val name: String)