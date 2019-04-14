package pt.pak3nuh.kafka.ui.service

import org.apache.kafka.common.KafkaFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> KafkaFuture<T>.await(): T {
    return suspendCoroutine { continuation ->
        whenComplete { value, err ->
            if (err != null) {
                continuation.resumeWithException(err)
            } else {
                continuation.resume(value)
            }
        }
    }
}
