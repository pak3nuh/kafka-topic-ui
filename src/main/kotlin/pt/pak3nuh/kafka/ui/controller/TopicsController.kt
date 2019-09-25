package pt.pak3nuh.kafka.ui.controller

import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerProviderService
import tornadofx.*

class TopicsController : Controller() {

    private val broker by param<Broker>()
    private val deserializerService by di<DeserializerProviderService>()

    suspend fun getTopics(): Sequence<Topic> {
        return broker.listTopics()
    }

    fun availableDeserializers(): Sequence<DeserializerMetadata> = deserializerService.availableDeserializers()

    val host: String = "${broker.host}:${broker.port}"

    suspend fun previewKeys(newTopic: Topic, metadata: DeserializerMetadata): List<String> {
        val records = broker.preview(newTopic)
        val deserializer = deserializerService.createDeserializer(metadata)
        return records.map {
            if (it.first == null) "null" else deserializer.deserialize(it.first!!)
        }
    }

    companion object {
        fun find(parent: Component, broker: Broker) =
                parent.find<TopicsController>(TopicsController::broker.name to broker)
    }

}