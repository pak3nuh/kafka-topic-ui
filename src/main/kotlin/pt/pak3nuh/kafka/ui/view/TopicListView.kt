package pt.pak3nuh.kafka.ui.view

import javafx.collections.transformation.FilteredList
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import pt.pak3nuh.kafka.ui.controller.TopicListController
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import pt.pak3nuh.kafka.ui.view.coroutine.onMain
import tornadofx.action
import tornadofx.attachTo
import tornadofx.borderpane
import tornadofx.button
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.label
import tornadofx.listview
import tornadofx.observableList
import tornadofx.onDoubleClick
import tornadofx.textfield
import tornadofx.titledpane
import tornadofx.vbox
import java.util.function.Predicate

private val logger = getSlfLogger<TopicListView>()

class TopicListView : CoroutineView("Topics") {

    private class ViewModel {
        val previewList = observableList<String>()
        var topicList = observableList<Topic>()
        val filteredList: FilteredList<Topic> = topicList.filtered { true }
        var keyDeserializer: DeserializerMetadata? = null
        var valueDeserializer: DeserializerMetadata? = null
        var selectedTopic: Topic? = null
    }

    private val viewModel = ViewModel()
    private val controller: TopicListController by inject()
    private val topicListView: ListView<Topic> = listview(viewModel.filteredList)

    override fun onCloseRequested() {
        controller.shutdownApp()
    }

    override val root = borderpane {

        title = "Topic list for broker ${controller.host}"

        center = vbox {

            titledpane("Topics") {
                topicListView.attachTo(this) {
                    selectionModel.selectionMode = SelectionMode.SINGLE
                    selectionModel.selectedItemProperty().addListener { _, _, newValue: Topic? ->
                        logger.debug("Changed selected topic to {}", newValue)
                        viewModel.selectedTopic = newValue
                        loadPreview()
                    }
                    onDoubleClick {
                        val topic: Topic = viewModel.selectedTopic ?: kotlin.error("No topic selected")
                        val detailView = TopicDetailView.create(
                                scope,
                                topic,
                                viewModel.keyDeserializer ?: kotlin.error("No key deserializer"),
                                viewModel.valueDeserializer ?: kotlin.error("No value deserializer")
                        )
                        detailView.openWindow()
                    }
                }

                borderpane {
                    center = textfield {
                        promptText = "Filter here"
                        textProperty().addListener { _, _, newValue ->
                            viewModel.filteredList.predicate = Predicate { it.name.contains(newValue) }
                        }
                    }
                    right = button("Refresh") {
                        action {
                            fxLaunch(this) {
                                val list = controller.getTopics().toList()
                                onMain {
                                    viewModel.topicList.clear()
                                    viewModel.topicList.addAll(list)
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
                viewModel.valueDeserializer = deserializerList[0].metadata
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
            val records = controller.previewKeys(topic, deserializer, refresh)

            onMain {
                viewModel.previewList.clear()
                viewModel.previewList.addAll(records)
            }

        }
    }

}

private class ComboDeserializerItem(
        val metadata: DeserializerMetadata
) {
    override fun toString(): String = metadata.name
}