package pt.pak3nuh.kafka.ui.service

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

class BrokerServiceTest {

    private val service = BrokerService()

    @Test
    internal fun `test connect`() {
        val broker = service.connect("localhost", 9092)
        val job = GlobalScope.async {
            broker.listTopics()
        }
        val future = job.toFuture()
        val sequence = future.get()
    }
}

private fun <T> Deferred<T>.toFuture(): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    this.invokeOnCompletion {
        if(it == null) {
            future.complete(this.getCompleted())
        } else {
            future.completeExceptionally(it)
        }
    }
    return future
}
