package pt.pak3nuh.kafka.ui.view.coroutine

import javafx.event.EventHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import pt.pak3nuh.kafka.ui.app.kafkaUiApplication
import tornadofx.View
import kotlin.coroutines.CoroutineContext

/**
 * View with coroutine support for cancel events
 */
abstract class CoroutineView(name: String) : View(name), CoroutineScope {
    override val coroutineContext: CoroutineContext = kafkaUiApplication.coroutineContext +
            Job(kafkaUiApplication.parentJob) +
            CoroutineName("view-$name")

    final override fun onDock() {
        currentWindow?.onCloseRequest = EventHandler {
            onCloseRequested()
        }
    }

    protected open fun onCloseRequested() {
        coroutineContext.cancel()
    }
}