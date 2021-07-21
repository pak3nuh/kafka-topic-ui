package pt.pak3nuh.kafka.ui.service.broker

import org.apache.kafka.clients.admin.AdminClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.app.wrapEx
import pt.pak3nuh.kafka.ui.config.DiBeanRegister
import java.io.File

@Service
class BrokerService @Autowired constructor(
        private val diBeanRegister: DiBeanRegister
) {

    fun connect(
            host: String,
            port: Int,
            timeoutMs: Int = 5_000,
            securityCredentials: SecurityCredentials? = null
    ): Broker {
        return wrapEx {
            val config = mutableMapOf<String, Any>()
            config.putAll(mapOf(
                    "bootstrap.servers" to "$host:$port",
                    "group.id" to "kafka-topic-ui-app",
                    "request.timeout.ms" to timeoutMs
            ))
            securityCredentials?.let { config.putAll(mapOf(
                    "security.protocol" to "SSL",
                    "ssl.keystore.location" to it.keystore.absolutePath,
                    "ssl.keystore.password" to it.keystorePassword,
                    "ssl.truststore.location" to it.truststore.absolutePath,
                    "ssl.truststore.password" to it.truststorePassword
            )) }
            val adminClient = AdminClient.create(config)
            val broker = Broker(host, port, securityCredentials, adminClient)
            diBeanRegister.registerBroker(broker)
            broker
        }
    }

}

data class SecurityCredentials(
        val truststore: File,
        val keystore: File,
        val truststorePassword: String,
        val keystorePassword: String,
)