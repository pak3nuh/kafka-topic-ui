package pt.pak3nuh.kafka.ui.service

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine

class BrokerServiceTest {

    private val service = BrokerService()

    @Test
    internal fun `test connect`() {
        val broker = service.connect("192.168.99.100", 9092)
        val future = future {
            broker.listTopics()
        }
        val sequence = future.get()
    }
}

private fun <T> future(
        context: CoroutineContext = Dispatchers.Default,
        block: suspend () -> T
): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    block.startCoroutine(object: Continuation<T> {
        override val context: CoroutineContext = context

        override fun resumeWith(result: Result<T>) {
            when {
                result.isFailure -> future.completeExceptionally(result.exceptionOrNull())
                result.isSuccess -> future.complete(result.getOrNull())
            }
        }
    })
    return future
}