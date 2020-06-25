package pt.pak3nuh.kafka.ui.view

import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import pt.pak3nuh.kafka.ui.controller.TopicListController
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.view.coroutine.ScopedView
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import pt.pak3nuh.kafka.ui.view.coroutine.onMain
import tornadofx.*

private val logger = getSlfLogger<TopicListView>()

// todo when close doesn't kill app after selected one topic
class TopicListView : ScopedView("Topics") {

    // todo some of this state should be controller
    private class ViewModel {
        val observableTopics = observableList<Topic>()
        val previewList = observableList<String>()
        var topicFilter: (String) -> Boolean = { true }
        var topicList: List<Topic> = listOf()
        var keyDeserializer: DeserializerMetadata? = null
        var valueDeserializer: DeserializerMetadata? = null
        var selectedTopic: Topic? = null
    }

    private val viewModel = ViewModel()
    private val controller: TopicListController by inject()
    private val topicListView: ListView<Topic> = listview(viewModel.observableTopics)

    override val root = borderpane {

        title = "Topic list for broker ${controller.host}"

        center = vbox {

            titledpane("Topics") {
                topicListView.attachTo(this) {
                    selectionModel.selectionMode = SelectionMode.SINGLE
                    selectionModel.selectedItemProperty().addListener { _, _, newValue: Topic ->
                        logger.debug("Changed selected topic to {}", newValue)
                        viewModel.selectedTopic = newValue
                        loadPreview()
                    }
                }

                borderpane {
                    center = textfield {
                        promptText = "Filter here"
                        textProperty().addListener { _, _, newValue ->
                            viewModel.topicFilter = { it.contains(newValue) }
                            filterTopics()
                        }
                        style {
                            fitToWidth = true
                        }
                    }
                    right = button("Refresh") {
                        action {
                            fxLaunch(this) {
                                viewModel.topicList = controller.getTopics().toList()
                                onMain {
                                    filterTopics()
                                }
                            }
                        }
                    }
                }
            }

            titledpane("Preview") {
                listview(viewModel.previewList)
            }

            titledpane("Deserializers") {
                collapsibleProperty().value = true
                val deserializerList = controller.availableDeserializers().map { ComboDeserializerItem(it) }.toList()
                viewModel.keyDeserializer = deserializerList[0].metadata
                hbox {
                    label("Key:")
                    combobox(values = deserializerList) {
                        selectionModel.select(0)
                        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                            viewModel.keyDeserializer = newValue?.metadata
                            logger.debug("Changed key deserializer to {}", newValue?.metadata?.name)
                            loadPreview()
                        }
                    }

                    label("Value:")
                    combobox(values = deserializerList) {
                        selectionModel.select(0)
                        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                            viewModel.valueDeserializer = newValue?.metadata
                            logger.debug("Changed value deserializer to {}", newValue?.metadata?.name)
                        }
                    }
                }
            }

        }

    }

    private fun loadPreview(refresh: Boolean = false) {
        val topic = viewModel.selectedTopic ?: return
        val deserializer = viewModel.keyDeserializer ?: return
        fxLaunch(topicListView) {
            val records = controller
                    .previewKeys(topic, deserializer, refresh)

            onMain {
                viewModel.previewList.clear()
                viewModel.previewList.addAll(records)
            }

        }
    }

    private fun filterTopics() {
        viewModel.observableTopics.clear()
        viewModel.observableTopics.addAll(viewModel.topicList.filter { viewModel.topicFilter(it.name) })
    }


}

private class ComboDeserializerItem(
        val metadata: DeserializerMetadata
) {
    override fun toString(): String = metadata.name
}