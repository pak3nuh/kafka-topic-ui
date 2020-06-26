package pt.pak3nuh.kafka.ui.service

import kotlinx.coroutines.cancel
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.app.asKafkaUi
import pt.pak3nuh.kafka.ui.config.DiBeanRegister
import tornadofx.FX
import java.util.concurrent.CancellationException
import kotlin.system.exitProcess

@Service
class ApplicationService(
        private val diBeanRegister: DiBeanRegister
) {
    fun shutdown() {
        FX.application.asKafkaUi().apply {
            coroutineContext.cancel(CancellationException("Shutdown"))
            stop()
        }
        diBeanRegister.shutdown()
        exitProcess(0) // shouldn't need to call this
    }

}