package pt.pak3nuh.kafka.ui.view

import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import pt.pak3nuh.kafka.ui.controller.NO_TOPICS
import pt.pak3nuh.kafka.ui.controller.TopicsController
import pt.pak3nuh.kafka.ui.service.Broker
import tornadofx.*

class TopicsFragment(broker: Broker) : Fragment() {

    private val controller by inject<TopicsController>(scope, "broker" to broker)
    private var topicList by singleAssign<ListView<String>>()

    override val root = borderpane {
        top = hbox {
            label("Topic list")
            button("Refresh"){ action { controller.updateTopics(topicList.items) } }
        }
        center = hbox {
            topicList = listview()
            topicList.selectionModel.selectionMode = SelectionMode.SINGLE
            topicList.items.add(NO_TOPICS)
        }
        bottom = hbox {
            button("Open Topic") {
                action {
                    val selectedItem: String? = topicList.selectionModel.selectedItem
                    if (selectedItem != null) {
                        // todo launch topic detail
                    }
                }
            }
        }
    }
}
