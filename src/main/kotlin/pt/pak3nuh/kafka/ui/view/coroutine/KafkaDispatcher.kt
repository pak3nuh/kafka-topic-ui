package pt.pak3nuh.kafka.ui.view.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private val AVAILABLE_CPU = Runtime.getRuntime().availableProcessors()

/**
 * Dispatcher for blocking kafka operations
 */
object KafkaDispatcher : CoroutineDispatcher() {

    private val dispatcher = Executors.newFixedThreadPool(AVAILABLE_CPU - 1)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatcher.submit(block)
    }
}

/**
 * Scope for kafka coroutines
 */
class KafkaScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = KafkaDispatcher
}