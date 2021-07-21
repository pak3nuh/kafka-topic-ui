package pt.pak3nuh.kafka.ui.controller

import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Broker
import pt.pak3nuh.kafka.ui.service.broker.BrokerService
import pt.pak3nuh.kafka.ui.service.broker.SecurityCredentials
import tornadofx.Controller

private val logger = getSlfLogger<LoginController>()

class LoginController: Controller() {

    private val brokerService: BrokerService by di()

    suspend fun getBroker(host: String, port: String): Broker? {
        return tryConnect(host, port, null)
    }

    suspend fun getBrokerSsl(host: String, port: String, securityCredentials: SecurityCredentials?): Broker? {
        return tryConnect(host, port, securityCredentials)
    }

    private suspend fun tryConnect(host: String, port: String, securityCredentials: SecurityCredentials?): Broker? {
        logger.info("Connecting to $host:$port")
        val broker = brokerService.connect(host, port.toInt(), securityCredentials = securityCredentials)
        return if (broker.isAvailable())
            broker
        else {
            null
        }
    }

}