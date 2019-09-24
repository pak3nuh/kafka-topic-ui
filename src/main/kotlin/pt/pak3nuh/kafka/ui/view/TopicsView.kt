package pt.pak3nuh.kafka.ui.view

import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import pt.pak3nuh.kafka.ui.controller.TopicsController
import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.Topic
import pt.pak3nuh.kafka.ui.view.coroutine.continueOnMain
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import tornadofx.*

private val NO_TOPICS = Topic("No Topics")

class TopicsView : View() {

    private val broker by param<Broker>()
    private val controller by TopicsController.inject(this, broker)
    private var topicList by singleAssign<ListView<Topic>>()

    override val root = borderpane {
        val topics = observableList<Topic>()
        top = hbox {
            label("Topic list")
            button("Refresh") {
                action {
                    fxLaunch(this) {
                        val controllerTopics = controller.getTopics()
                        continueOnMain {
                            topics.clear()
                            topics.addAll(controllerTopics)

                            if (topics.isEmpty()) {
                                topics.add(NO_TOPICS)
                            }
                        }
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
                        TopicDetailFragment.find(this@TopicsView, broker, selectedItem).openWindow()
                    }
                }
            }
        }
    }

    companion object {
        fun find(parent: Component, broker: Broker) = parent.find<TopicsView>(
                TopicsView::broker to broker
        )
    }
}
