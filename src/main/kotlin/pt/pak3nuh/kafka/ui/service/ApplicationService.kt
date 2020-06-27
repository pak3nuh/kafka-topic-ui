package pt.pak3nuh.kafka.ui.service

import javafx.application.Platform
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.config.DiBeanRegister
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = getSlfLogger<ApplicationService>()

@Service
class ApplicationService(
        private val diBeanRegister: DiBeanRegister
) {
    fun shutdown() {
        logger.info("Invoking application shutdown")
        diBeanRegister.shutdown()
        Platform.exit()
        // shouldn't need to do this
        thread(isDaemon = true) {
            Thread.sleep(3_000)
            logger.info("Forcing process exit")
            exitProcess(0)
        }
    }

}