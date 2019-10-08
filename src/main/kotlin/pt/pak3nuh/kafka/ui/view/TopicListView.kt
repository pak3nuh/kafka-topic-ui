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

class TopicListView : ScopedView("Topics") {

    private var topicFilter: (String) -> Boolean = { true }
    private var topicList: List<Topic> = listOf()
    private var keyDeserializer: DeserializerMetadata? = null
    private var valueDeserializer: DeserializerMetadata? = null
    private lateinit var selectedTopic: Topic

    private val controller: TopicListController by di()
    private val observableTopics = observableList<Topic>()
    private val topicListView: ListView<Topic> = listview(observableTopics)
    private val previewList = observableList<String>()
    private val previewRefreshButton = button("Refresh Preview")

    override val root = borderpane {

        title = "Topic list for broker ${controller.host}"

        top = hbox {
            button("Load Topics") {
                action {
                    fxLaunch(this) {
                        topicList = controller.getTopics().toList()
                        onMain {
                            filterTopics()
                        }
                    }
                }
            }

            button("Open Topic") {
                action {
                    val selectedItem: Topic? = topicListView.selectionModel.selectedItem
                    if (selectedItem != null) {
                        //todo detail
                    }
                }
            }
        }
        center = hbox {

            // topic list
            vbox {
                textfield {
                    promptText = "Filter topics"
                    textProperty().addListener { _, _, newValue ->
                        topicFilter = { it.contains(newValue) }
                        filterTopics()
                    }
                }

                topicListView.attachTo(this)
                topicListView.selectionModel.selectionMode = SelectionMode.SINGLE
                topicListView.selectionModel.selectedItemProperty().addListener { _, _, newValue: Topic ->
                    logger.debug("Changed selected topic to {}", newValue)
                    selectedTopic = newValue
                    loadPreview()
                }
            }

            // preview
            vbox {
                // deserializers
                hbox {
                    val deserializerList = controller.availableDeserializers().map { ComboDeserializerItem(it) }.toList()
                    keyDeserializer = deserializerList[0].metadata
                    vbox {
                        fieldset {
                            label("Key Deserializer")
                            combobox(values = deserializerList) {
                                selectionModel.select(0)
                                selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                    keyDeserializer = newValue?.metadata
                                    logger.debug("Changed key deserializer to {}", newValue?.metadata?.name)
                                    loadPreview()
                                }
                            }
                        }
                    }
                    vbox {
                        fieldset {
                            label("Value Deserializer")
                            combobox(values = deserializerList) {
                                selectionModel.select(0)
                                selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                    valueDeserializer = newValue?.metadata
                                    logger.debug("Changed value deserializer to {}", newValue?.metadata?.name)
                                }
                            }
                        }
                    }
                }
                add(previewRefreshButton)
                previewRefreshButton.action {
                    loadPreview(true)
                }


                // top 5 messages
                vbox {
                    label("Topic key preview")
                    listview(previewList)
                }
            }
        }

    }

    private fun loadPreview(refresh: Boolean = false) {
        val deserializer = keyDeserializer ?: return
        fxLaunch(topicListView, previewRefreshButton) {
            val records = controller
                .previewKeys(selectedTopic, deserializer, refresh)

            onMain {
                previewList.clear()
                previewList.addAll(records)
            }

        }
    }


    private fun filterTopics() {
        observableTopics.clear()
        observableTopics.addAll(topicList.filter { topicFilter(it.name) })
    }

    companion object {
        fun find(parent: Component, controller: TopicListController) = parent.find<TopicListView>(
            TopicListView::controller to controller
        )
    }
}

private class ComboDeserializerItem(
    val metadata: DeserializerMetadata
) {
    override fun toString(): String = metadata.name
}