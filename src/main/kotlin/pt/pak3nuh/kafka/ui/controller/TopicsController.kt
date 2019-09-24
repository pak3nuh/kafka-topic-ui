package pt.pak3nuh.kafka.ui.controller

import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.Topic
import tornadofx.*

class TopicsController : Controller() {

    private val broker by param<Broker>()

    suspend fun getTopics(): Sequence<Topic> {
        return broker.listTopics()
    }


    companion object {
        fun inject(parent: Component, broker: Broker) =
                parent.inject<TopicsController>(parent.scope, TopicsController::broker.name to broker)
    }

}