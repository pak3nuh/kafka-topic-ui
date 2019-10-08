package pt.pak3nuh.kafka.ui.service.broker

import org.apache.kafka.clients.admin.AdminClient
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.app.wrapEx
import pt.pak3nuh.kafka.ui.log.getSlfLogger

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