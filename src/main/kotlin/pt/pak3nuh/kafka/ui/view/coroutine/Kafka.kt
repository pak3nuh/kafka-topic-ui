package pt.pak3nuh.kafka.ui.view.coroutine

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Runnable
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private val logger = getSlfLogger<KafkaDispatcher>()
private val AVAILABLE_CPU = Runtime.getRuntime().availableProcessors()

/**
 * Dispatcher for blocking kafka operations
 */
object KafkaDispatcher : ExecutorCoroutineDispatcher() {

    private val service: ExecutorService = Executors.newFixedThreadPool(AVAILABLE_CPU - 1)
    override val executor: Executor
        get() = service

    override fun close() {
        service.shutdown()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        // the documentation states that the method should be exception free
        // but says nothing about exceptions thrown by the runnable
        // should be handled by the global handler
        try {
            service.submit(block)
        } catch (e: Exception) {
            logger.error("Exception thrown running the a coroutine", e)
            throw e
        }
    }
}