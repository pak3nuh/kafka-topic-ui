package pt.pak3nuh.kafka.ui.config

import org.springframework.context.support.GenericApplicationContext
import org.springframework.stereotype.Service
import pt.pak3nuh.kafka.ui.service.broker.Broker

/**
 * For contextualized beans
 */
@Service
class DiBeanRegister(private val context: GenericApplicationContext) {

    fun registerBroker(broker: Broker) {
        if (context.containsBean(BROKER_BEAN_NAME)) {
            context.removeBeanDefinition(BROKER_BEAN_NAME)
        }
        context.registerBean(BROKER_BEAN_NAME, Broker::class.java, { broker }, emptyArray())
    }

    fun shutdown() {
        context.close()
    }

    companion object {
        private val BROKER_BEAN_NAME = Broker::class.qualifiedName!!
    }
}