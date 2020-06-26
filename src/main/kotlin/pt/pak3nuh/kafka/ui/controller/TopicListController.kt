package pt.pak3nuh.kafka.ui.controller

import pt.pak3nuh.kafka.ui.service.ApplicationService
import pt.pak3nuh.kafka.ui.service.broker.Broker
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerProviderService
import tornadofx.Controller

class TopicListController : Controller() {

    private val broker by di<Broker>()
    private val deserializerService by di<DeserializerProviderService>()
    private val applicationService by di<ApplicationService>()

    suspend fun getTopics(): Sequence<Topic> {
        return broker.listTopics()
    }

    fun availableDeserializers(): Sequence<DeserializerMetadata> = deserializerService.availableDeserializers()

    val host: String = "${broker.host}:${broker.port}"

    suspend fun previewKeys(newTopic: Topic, metadata: DeserializerMetadata, refresh: Boolean): List<String> {
        val records = broker.preview(newTopic, refresh)
        val deserializer = deserializerService.createDeserializer(metadata)
        return records.map {
            if (it.first == null) "null" else deserializer.deserialize(it.first!!)
        }
    }

    fun close() {
        applicationService.shutdown()
    }

}