package pt.pak3nuh.kafka.ui.service.consumer

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import pt.pak3nuh.kafka.ui.service.broker.SecurityCredentials
import java.util.*

fun createConsumerProperties(
    servers: String,
    groupId: String,
    securityCredentials: SecurityCredentials?,
    earliest: Boolean = true,
    autoCommit: Boolean = true
): Properties {
    val props = Properties()
    props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.qualifiedName
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.qualifiedName
    props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
    props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = if (earliest) "earliest" else "latest"
    props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = autoCommit
    securityCredentials?.let {
        props.putAll(it.getAsMap())
    }
    return props
}

fun createProducerProperties(
        servers: String,
        securityCredentials: SecurityCredentials?
): Properties {
    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.qualifiedName
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.qualifiedName
    securityCredentials?.let {
        props.putAll(it.getAsMap())
    }
    return props
}

typealias BytesConsumer = Consumer<ByteArray, ByteArray>