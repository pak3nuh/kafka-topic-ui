package pt.pak3nuh.kafka.ui.view

import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import pt.pak3nuh.kafka.ui.controller.NO_TOPICS
import pt.pak3nuh.kafka.ui.controller.TopicsController
import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.Topic
import pt.pak3nuh.kafka.ui.view.coroutine.launch
import tornadofx.*

class TopicsFragment(broker: Broker) : Fragment() {

    private val controller by TopicsController.inject(this, broker)
    private var topicList by singleAssign<ListView<Topic>>()

    override val root = borderpane {
        val topics = observableList<Topic>()
        top = hbox {
            label("Topic list")
            button("Refresh") {
                action {
                    launch(this) {
                        controller.updateTopics(topics)
                    }
                }
            }
        }
        center = hbox {
            topicList = listview(topics)
            topicList.selectionModel.selectionMode = SelectionMode.SINGLE
            topicList.items.add(NO_TOPICS)
        }
        bottom = hbox {
            button("Open Topic") {
                action {
                    val selectedItem: Topic? = topicList.selectionModel.selectedItem
                    if (selectedItem != null) {
                        TopicDetailFragment.find(this@TopicsFragment, broker, selectedItem).openWindow()
                    }
                }
            }
        }
    }
}
