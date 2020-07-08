package pt.pak3nuh.kafka.ui.service.broker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerProviderService

@Lazy
@Component
class SubscriptionService @Autowired constructor(
        private val broker: Broker,
        private val deserializerProviderService: DeserializerProviderService
) {
    fun subscribe(keyDeserializer: DeserializerMetadata, valueDeserializer: DeserializerMetadata, topic: Topic, earliest: Boolean): Subscription {
        val key = deserializerProviderService.createDeserializer(keyDeserializer)
        val value = deserializerProviderService.createDeserializer(valueDeserializer)
        val subscription = Subscription(key, value, topic, broker.host, broker.port, earliest)
        subscription.initSync()
        return subscription
    }
}