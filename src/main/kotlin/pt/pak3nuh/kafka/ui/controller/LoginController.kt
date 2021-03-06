package pt.pak3nuh.kafka.ui.controller

import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Broker
import pt.pak3nuh.kafka.ui.service.broker.BrokerService
import tornadofx.Controller

private val logger = getSlfLogger<LoginController>()

class LoginController: Controller() {

    private val brokerService: BrokerService by di()

    suspend fun getBroker(host: String, port: String): Broker? {
        return tryConnect(host, port)
    }

    private suspend fun tryConnect(host: String, port: String): Broker? {
        logger.info("Connecting to $host:$port")
        val broker = brokerService.connect(host, port.toInt())
        return if (broker.isAvailable())
            broker
        else {
            null
        }
    }

}