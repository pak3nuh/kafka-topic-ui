package pt.pak3nuh.kafka.ui.service.deserializer

import kotlin.reflect.KClass

interface DeserializerProvider {
    val deserializers: Sequence<DeserializerMetadata>
    fun <T : Deserializer> createDeserializer(type: KClass<T>, vararg params: Parameter): T
}

typealias Parameter = Pair<Property, String>