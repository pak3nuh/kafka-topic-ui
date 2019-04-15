package pt.pak3nuh.kafka.ui.view

import pt.pak3nuh.kafka.ui.controller.TopicsController
import pt.pak3nuh.kafka.ui.service.Broker
import tornadofx.*

class TopicsFragment(broker: Broker) : Fragment() {

    private val controller by inject<TopicsController>(scope, "broker" to broker)

    override val root = borderpane {
        top = hbox {
            label("Topic list")
            val list = combobox<String>()
            controller.updateTopics(list.items)
            button("Refresh"){ action { controller.updateTopics(list.items) } }
        }
    }
}
