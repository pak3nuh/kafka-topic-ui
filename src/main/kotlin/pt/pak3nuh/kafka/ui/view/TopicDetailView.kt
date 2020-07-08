package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
import javafx.scene.control.ToggleGroup
import javafx.util.StringConverter
import kotlinx.coroutines.launch
import pt.pak3nuh.kafka.ui.app.copy
import pt.pak3nuh.kafka.ui.controller.TopicDetailController
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import pt.pak3nuh.kafka.ui.view.coroutine.onMain
import tornadofx.Scope
import tornadofx.action
import tornadofx.bind
import tornadofx.borderpane
import tornadofx.button
import tornadofx.find
import tornadofx.hbox
import tornadofx.observableList
import tornadofx.radiobutton
import tornadofx.readonlyColumn
import tornadofx.tableview

class TopicDetailView : CoroutineView("Topic Detail") {

    private val model: Model = Model()
    private val topic: Topic by param()
    private val controller: TopicDetailController by inject(params = params.plus(sequenceOf(
            TopicDetailController::model.name to model
    )))

    override val root: Parent = borderpane {
        title = topic.name
        // start pause seek settings assignment
        top = hbox {
            val group = ToggleGroup()
            radiobutton("Earliest", group) {
                action {
                    model.startOnEarliest = true
                }
            }
            radiobutton("Latest", group){
                isSelected = true
                action {
                    model.startOnEarliest = false
                }
            }
            button("Start") {
                bind(model.status, converter = model.statusConverter)
                action {
                    when (model.status.get()) {
                        Model.Status.NotStarted -> startLoop()
                        Model.Status.Active -> pauseLoop()
                        Model.Status.Paused -> resumeLoop()
                    }
                }
            }
            button("Clear") {
                action {
                    model.recordList.clear()
                }
            }
        }

        center = tableview(model.recordList) {
            readonlyColumn("Key", Record::key)
            readonlyColumn("Value", Record::value)
        }
    }

    private fun resumeLoop() {
        controller.resume()
        model.status.value = Model.Status.Active
    }

    private fun pauseLoop() {
        controller.pause()
        model.status.value = Model.Status.Paused
    }

    private fun startLoop() {
        launch {
            controller.start { records ->
                val mapped = records.map { Record(it.first ?: "", it.second ?: "") }
                onMain {
                    model.recordList.addAll(mapped)
                }
            }
            onMain {
                model.status.value = Model.Status.Active
            }
        }
    }

    override fun onCloseRequested() {
        super.onCloseRequested()
        controller.close()
    }

    class Model {
        var startOnEarliest: Boolean = false
        val recordList = observableList<Record>()
        val status = SimpleObjectProperty<Status>(Status.NotStarted)
        val statusConverter = object : StringConverter<Status>() {
            override fun toString(value: Status): String = value.text

            override fun fromString(string: String?): Status {
                TODO("Not yet implemented")
            }
        }

        enum class Status(val text: String) {
            NotStarted("Start"), Active("Pause"), Paused("Resume")
        }
    }

    companion object {
        fun build(scope: Scope, topic: Topic, keyDeserializer: DeserializerMetadata, valueDeserializer: DeserializerMetadata) =
                find(
                        TopicDetailView::class,
                        scope.copy(), // todo can be improved to reuse shared parts of the scope
                        TopicDetailController::topic to topic,
                        TopicDetailController::keyMetadata to keyDeserializer,
                        TopicDetailController::valueMetadata to valueDeserializer
                )
    }

    data class Record(val key: String, val value: String)
}