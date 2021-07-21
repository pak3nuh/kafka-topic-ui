package pt.pak3nuh.kafka.ui.service.broker

import pt.pak3nuh.kafka.ui.service.deserializer.Deserializer

data class KafkaRecord(val key: ByteArray?,
                       val value: ByteArray?,
                       private val keyDeserializer: Deserializer,
                       private val valueDeserializer: Deserializer) {

    fun deserializeKey(): String? = key?.let(keyDeserializer::deserialize)
    fun deserializeValue(): String? = value?.let(valueDeserializer::deserialize)

}