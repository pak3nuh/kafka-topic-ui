package pt.pak3nuh.kafka.ui.service

import kotlinx.coroutines.withContext
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.consumer.BytesConsumer
import pt.pak3nuh.kafka.ui.view.coroutine.KafkaDispatcher
import java.time.Duration

private val logger = getSlfLogger<PreviewCache>()

class PreviewCache(private val consumer: BytesConsumer) {

    private val cache = mutableMapOf<Topic, ByteList>()

    suspend fun get(topic: Topic, numberRecords: Int): ByteList {
        return cache[topic] ?: refresh(topic, numberRecords)
    }

    suspend fun refresh(topic: Topic, numberRecords: Int): ByteList {
        val fetch = fetch(topic, numberRecords)
        cache[topic] = fetch
        return fetch
    }

    fun clear(): Unit = cache.clear()

    private suspend fun fetch(topic: Topic, numberRecords: Int): ByteList {
        return withContext(KafkaDispatcher) {
            val topicName = topic.name
            logger.debug("Previewing topic {}", topicName)
            consumer.subscribe(listOf(topicName))
            val records = consumer.poll(Duration.ofSeconds(5))
            logger.trace("Got {} records", records.count())
            val result = records.map {
                Pair(it.key(), it.value())
            }.take(numberRecords)
            consumer.unsubscribe()
            result
        }
    }

}

typealias ByteList = List<Pair<ByteArray, ByteArray>>