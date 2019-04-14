package pt.pak3nuh.kafka.ui.controller

import pt.pak3nuh.kafka.ui.service.Broker
import tornadofx.*

class TopicsController(private val broker: Broker) : Controller() {

    suspend fun fillTopics(): Collection<ListMenuItem> {
        return broker.listTopics()
                .map { ListMenuItem(it.name) }
                .toList()
    }


}