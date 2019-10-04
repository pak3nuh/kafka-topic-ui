package pt.pak3nuh.kafka.ui.view.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import tornadofx.*
import kotlin.coroutines.CoroutineContext

/**
 * View with coroutine support for cancel events
 */
abstract class ScopedView(name: String) : View(name), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = job + CoroutineName(name)
    override fun onDelete() {
        job.cancel()
    }
}