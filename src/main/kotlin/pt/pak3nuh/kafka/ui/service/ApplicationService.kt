package pt.pak3nuh.kafka.ui.service

import javafx.application.Platform
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.config.DiBeanRegister
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = getSlfLogger<ApplicationService>()

@Service
class ApplicationService {
    fun shutdown() {
        logger.info("Invoking application shutdown")
        Platform.exit()
    }
}