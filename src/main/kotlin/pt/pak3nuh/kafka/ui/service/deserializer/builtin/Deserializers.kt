package pt.pak3nuh.kafka.ui.service.deserializer.builtin

import pt.pak3nuh.kafka.ui.service.deserializer.Deserializer
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerProvider
import pt.pak3nuh.kafka.ui.service.deserializer.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class BuiltinDeserializerProvider : DeserializerProvider {

    override fun <T : Deserializer> createDeserializer(type: KClass<T>, vararg params: Parameter): T {
        return when (type) {
            StringDeserializer::class -> createInstance(type)
            ByteArrayDeserializer::class -> createInstance(type)
            else -> throw IllegalArgumentException("Type $type not supported")
        }
    }

    private fun <T : Deserializer> createInstance(type: KClass<T>): T {
        return type.primaryConstructor?.call()
                ?: throw IllegalStateException("Type $type doesn't have a primary no args constructor")
    }

    override val deserializers: Sequence<DeserializerMetadata>
        get() = sequenceOf(
                DeserializerMetadata("Bytes", emptySequence(), ByteArrayDeserializer::class),
                DeserializerMetadata("String UTF8", emptySequence(), StringDeserializer::class)
        )
}

class StringDeserializer : Deserializer {

    override fun deserialize(bytes: ByteArray): String {
        return String(bytes, Charsets.UTF_8)
    }
}

class ByteArrayDeserializer : Deserializer {
    override fun deserialize(bytes: ByteArray): String {
        return bytes.contentToString()
    }
}