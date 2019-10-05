package pt.pak3nuh.kafka.ui.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.kafka.common.KafkaFuture
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.startCoroutine

suspend fun <T> KafkaFuture<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            this.cancel(true)
        }
        whenComplete { value, err ->
            if (err != null) {
                continuation.resumeWithException(err)
            } else {
                continuation.resume(value)
            }
        }
    }
}

fun <T> future(
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