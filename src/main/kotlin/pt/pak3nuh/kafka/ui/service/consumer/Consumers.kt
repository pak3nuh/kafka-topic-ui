package pt.pak3nuh.kafka.ui.service.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import java.util.Properties

fun createConsumerProperties(
        servers: String,
        groupId: String
): Properties {
    val props = Properties()
    props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.qualifiedName
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.qualifiedName
    props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
    return props
}