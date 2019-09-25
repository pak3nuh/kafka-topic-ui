package pt.pak3nuh.kafka.ui.service.deserializer

import kotlin.reflect.KClass

interface Deserializer {
    fun deserialize(bytes: ByteArray): String
}

data class DeserializerMetadata(
        val name: String,
        val properties: Sequence<Property>,
        val clazz: KClass<out Deserializer>
)

class Property(
        val name: String,
        val propertyValidator: PropertyValidator,
        val defaultValue: String?
) {
    val isMandatory: Boolean
        get() = defaultValue == null
}

interface PropertyValidator {
    fun validate(property: String): Boolean
    val validationMessage: String
}
