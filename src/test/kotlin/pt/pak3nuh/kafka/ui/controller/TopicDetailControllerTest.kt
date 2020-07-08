package pt.pak3nuh.kafka.ui.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TopicDetailControllerTest {
    @Test
    internal fun `structured concurrency`() {
        runBlocking {
            val job = withContext(coroutineContext + Dispatchers.IO) {
                launch {
                    delay(5_000)
                }
            }
            Assertions.assertEquals(job.isActive, false)
        }
    }
}