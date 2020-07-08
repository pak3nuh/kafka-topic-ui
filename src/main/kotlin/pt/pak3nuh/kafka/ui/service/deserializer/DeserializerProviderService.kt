package pt.pak3nuh.kafka.ui.service.deserializer

import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import java.util.ServiceLoader

private val logger = getSlfLogger<DeserializerProviderService>()

@Service
open class DeserializerProviderService {

    private val providers: List<DeserializerProvider> = ServiceLoader.load(DeserializerProvider::class.java).toList()

    init {
        logger.debug("Loading deserializer providers {}", providers)
    }

    fun availableDeserializers(): Sequence<DeserializerMetadata> =
            providers.asSequence().flatMap { it.deserializers.asSequence() }

    fun createDeserializer(metadata: DeserializerMetadata): Deserializer {
        return providers.first { metadata in it.deserializers }.createDeserializer(metadata.clazz)
    }

}