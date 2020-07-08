package pt.pak3nuh.kafka.ui.app

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import pt.pak3nuh.kafka.ui.injector.SpringContainer
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.view.LoginView
import tornadofx.App
import tornadofx.FX
import tornadofx.Scope
import tornadofx.launch
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

private val logger = getSlfLogger<KafkaUiApp>()

/**
 * Convenience field to store the application after initialized
 */
lateinit var kafkaUiApplication: KafkaUiApp

/**
 * Unique ID per application so that consumer groups don't clash
 */
val applicationUUID = UUID.randomUUID().toString()

class KafkaUiApp : App(LoginView::class, Styles::class, Scope()), CoroutineScope {
    val parentJob = SupervisorJob()
    val kafkaScope = KafkaScope(parentJob)
    private val counter = AtomicInteger(1)
    private val threadPool = Executors.newFixedThreadPool(2) {
        Thread(it, "kafka-ui-coroutine-${counter.getAndIncrement()}").apply {
            isDaemon = true
        }
    }
    override val coroutineContext: CoroutineContext = parentJob +
            threadPool.asCoroutineDispatcher() +
            CoroutineName("application-coroutine")

    override fun stop() {
        logger.info("Stopping application")
        super.stop()
        coroutineContext.cancel()
        threadPool.shutdown()
        (FX.dicontainer as SpringContainer).close()
        logger.debug("Application stopped")
    }

    override fun init() {
        logger.info("Initializing application")
        super.init()
        FX.dicontainer = SpringContainer()
        kafkaUiApplication = this
        logger.debug("Application initialized")
    }
}

class KafkaScope(parent: Job): CoroutineScope {
    private val parentJob = SupervisorJob(parent)
    override val coroutineContext: CoroutineContext = parentJob +
            Dispatchers.IO +
            CoroutineName("kafka-coroutine")
}

fun main(args: Array<String>) {
    launch<KafkaUiApp>(args)
}
