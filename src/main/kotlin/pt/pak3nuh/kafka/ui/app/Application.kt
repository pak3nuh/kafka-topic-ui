package pt.pak3nuh.kafka.ui.app

import javafx.application.Application
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import pt.pak3nuh.kafka.ui.injector.SpringContainer
import pt.pak3nuh.kafka.ui.view.LoginView
import tornadofx.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class KafkaUiApp : App(LoginView::class, Styles::class, Scope()), CoroutineScope {
    val parentJob = SupervisorJob()
    private val counter = AtomicInteger(1)
    override val coroutineContext: CoroutineContext = parentJob +
            Executors.newFixedThreadPool(2) {
                Thread(it, "kafka-ui-coroutine-${counter.getAndIncrement()}")
            }.asCoroutineDispatcher() +
            CoroutineName("application")

}

fun main(args: Array<String>) {
    FX.dicontainer = SpringContainer()
    launch<KafkaUiApp>(args)
}

fun Application.asKafkaUi() = this as KafkaUiApp
