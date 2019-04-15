package pt.pak3nuh.kafka.ui.controller

import javafx.collections.ObservableList
import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.Topic
import tornadofx.*

val NO_TOPICS = Topic("No Topics")

class TopicsController : Controller() {

    private val broker: Broker by params

    suspend fun updateTopics(list: ObservableList<Topic>) {
        val topics = broker.listTopics()

        list.clear()
        topics.forEach { list.add(it) }

        if (list.isEmpty()) {
            list.add(NO_TOPICS)
        }
    }


    companion object {
        fun inject(parent: Component, broker: Broker) =
                parent.inject<TopicsController>(parent.scope, "broker" to broker)
    }

}