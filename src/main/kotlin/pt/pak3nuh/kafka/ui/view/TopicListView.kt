package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.ToggleGroup
import pt.pak3nuh.kafka.ui.config.SettingsConfig
import pt.pak3nuh.kafka.ui.controller.TopicListController
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import pt.pak3nuh.kafka.ui.view.coroutine.onMain
import tornadofx.*
import java.util.function.Predicate
import kotlin.error

private val logger = getSlfLogger<TopicListView>()

class TopicListView : CoroutineView("Topics") {

    private class ViewModel {
        val previewList = observableList<String>()
        var topicList = observableList<Topic>()
        val filteredList: FilteredList<Topic> = topicList.filtered { true }
        var keyDeserializer: DeserializerMetadata? = null
        var valueDeserializer: DeserializerMetadata? = null
        var selectedTopic: Property<Topic?> = SimpleObjectProperty()
    }

    private val viewModel = ViewModel()
    private val controller: TopicListController by inject()
    private val settings: SettingsConfig by di()
    private val topicListView: ListView<Topic> = listview(viewModel.filteredList)

    override fun onCloseRequested() {
        controller.shutdownApp()
    }

    override val root = borderpane {

        title = "Topic list for broker ${controller.host}"

        center = vbox {

            titledpane("Topics (requires list topic permission)") {
                isCollapsible = false
                topicListView.attachTo(this) {
                    selectionModel.selectionMode = SelectionMode.SINGLE
                    selectionModel.selectedItemProperty().addListener { _, _, newValue: Topic? ->
                        logger.debug("Changed selected topic to {}", newValue)
                        viewModel.selectedTopic.value = newValue
                        loadPreview()
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
                            fxLaunch(this) { refreshTopics() }
                        }
                    }
                }
            }

            titledpane("Data") {
                expandedProperty().bindBidirectional(viewModel.selectedTopic.asBoolean { it != null })
                val deserializerList = controller.availableDeserializers().map { ComboDeserializerItem(it) }.toList()
                viewModel.keyDeserializer = deserializerList[0].metadata
                viewModel.valueDeserializer = deserializerList[0].metadata
                vbox {
                    spacing = 10.0
                    val toggleGroup = ToggleGroup()
                    val isCustomTopicNameEnabled = SimpleBooleanProperty()

                    radiobutton("List selection", toggleGroup) {
                        isSelected = true
                        action {
                            viewModel.selectedTopic.value = null
                            isCustomTopicNameEnabled.value = false
                        }
                    }
                    hbox {
                        val customTopicName = SimpleStringProperty()
                        customTopicName.onChange {
                            viewModel.selectedTopic.value = Topic(it ?: "")
                        }
                        radiobutton("With topic name", toggleGroup) {
                            action {
                                isCustomTopicNameEnabled.value = true
                            }
                        }
                        textfield("Custom topic name") {
                            this.enableWhen(isCustomTopicNameEnabled)
                            this.bind(customTopicName)
                        }
                    }
                }
                hbox {
                    spacing = 10.0
                    val enabled = viewModel.selectedTopic.asBoolean { new -> new != null }
                    vbox {
                        label("Key:")
                        combobox(values = deserializerList) {
                            enableWhen(enabled)
                            selectionModel.select(0)
                            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                viewModel.keyDeserializer = newValue?.metadata
                                logger.debug("Changed key deserializer to {}", newValue?.metadata?.name)
                                loadPreview()
                            }
                        }
                    }
                    vbox {
                        label("Value:")
                        combobox(values = deserializerList) {
                            enableWhen(enabled)
                            selectionModel.select(0)
                            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                viewModel.valueDeserializer = newValue?.metadata
                                logger.debug("Changed value deserializer to {}", newValue?.metadata?.name)
                            }
                        }
                    }
                    vbox {
                        alignment = Pos.BOTTOM_CENTER
                        button("Open topic view") {
                            enableWhen(enabled)
                            action {
                                val topic: Topic = viewModel.selectedTopic.value ?: error("No topic selected")
                                val detailView = TopicDetailView.create(
                                        scope,
                                        topic,
                                        viewModel.keyDeserializer ?: error("No key deserializer"),
                                        viewModel.valueDeserializer ?: error("No value deserializer")
                                )
                                detailView.openWindow()
                            }
                        }

                    }
                }

            }

            titledpane("Selected topic keys preview") {
                isExpanded = false
                listview(viewModel.previewList)
            }

            fxLaunch { refreshTopics() }
        }

    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        settings.configureDefaults(this)
    }

    private suspend fun refreshTopics() {
        val list = controller.getTopics().toList()
        onMain {
            viewModel.topicList.clear()
            viewModel.topicList.addAll(list)
        }
    }

    private fun loadPreview(refresh: Boolean = false) {
        val topic = viewModel.selectedTopic.value ?: return
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
