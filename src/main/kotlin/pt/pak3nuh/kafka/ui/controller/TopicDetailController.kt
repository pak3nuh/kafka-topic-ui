package pt.pak3nuh.kafka.ui.controller

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.pak3nuh.kafka.ui.app.kafkaUiApplication
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.KafkaRecord
import pt.pak3nuh.kafka.ui.service.broker.Subscription
import pt.pak3nuh.kafka.ui.service.broker.SubscriptionService
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.view.TopicDetailView
import tornadofx.Controller
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext

private val logger = getSlfLogger<TopicDetailController>()

typealias OnRecords = suspend (List<KafkaRecord>) -> Unit

class TopicDetailController : Controller() {
    val topic: Topic by param()
    val keyMetadata: DeserializerMetadata by param()
    val valueMetadata: DeserializerMetadata by param()
    val model: TopicDetailView.Model by param()
    private val subscriptionService: SubscriptionService by di()
    private var job: Job? = null
    private var running: Boolean = true

    suspend fun start(onRecords: OnRecords) {
        logger.info("Starting subscription on topic {}", topic)
        val subscription = subscriptionService.subscribe(keyMetadata, valueMetadata, topic)
        job = kafkaUiApplication.kafkaScope.launch {
            if (model.startOnEarliest) {
                subscription.seekWhenReady(0)
            }
            poll(subscription, onRecords)
        }
        job?.invokeOnCompletion {
            logger.info("Closing subscription")
            subscription.close()
        }
    }

    private suspend fun poll(subscription: Subscription, onRecords: OnRecords) {
        while (coroutineContext.isActive) {
            logger.trace("Job active on topic {}", topic)
            if (running) {
                logger.debug("Fetching records on topic {}", topic)
                val recordsList = subscription.pollSync(Duration.of(1, ChronoUnit.SECONDS)).toList()
                onRecords(recordsList)
            }
            delay(1_000)
        }
        logger.info("Exiting poll loop")
    }

    fun pause() {
        logger.debug("Pausing subscription on topic {}", topic)
        running = false
    }

    fun resume() {
        logger.debug("Resuming subscription on topic {}", topic)
        running = true
    }

    fun close() {
        logger.debug("Canceling subscription on topic {}", topic)
        job?.cancel()
    }

    suspend fun sendRecord(keyFile: File?, valueFile: File?) {
        subscriptionService.sendRecord(
                topic,
                keyFile?.readBytes(),
                valueFile?.readBytes()
        )
    }
}