package pt.pak3nuh.kafka.ui.config

import org.springframework.context.support.GenericApplicationContext
import org.springframework.stereotype.Component
import pt.pak3nuh.kafka.ui.service.broker.Broker

@Component
class DiBeanRegister(private val context: GenericApplicationContext) {

    fun registerBroker(broker: Broker) {
        context.registerBean(Broker::class.qualifiedName, Broker::class.java, { broker }, emptyArray())
    }

}