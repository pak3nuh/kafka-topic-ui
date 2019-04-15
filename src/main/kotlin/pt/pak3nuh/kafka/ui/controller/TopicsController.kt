package pt.pak3nuh.kafka.ui.controller

import javafx.collections.ObservableList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pt.pak3nuh.kafka.ui.service.Broker
import tornadofx.*

class TopicsController : Controller() {

    private val broker: Broker by params

    fun updateTopics(list:ObservableList<String>) {
        GlobalScope.launch {
            val topics = broker.listTopics()

            list.clear()
            topics.forEach { list.add(it.name) }

            if(list.isEmpty()) {
                list.add("No topics")
            }
        }
    }


}