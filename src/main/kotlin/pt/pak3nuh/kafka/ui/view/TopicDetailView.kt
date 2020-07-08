package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.Parent
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.util.StringConverter
import kotlinx.coroutines.launch
import pt.pak3nuh.kafka.ui.app.copy
import pt.pak3nuh.kafka.ui.controller.TopicDetailController
import pt.pak3nuh.kafka.ui.service.broker.Topic
import pt.pak3nuh.kafka.ui.service.deserializer.DeserializerMetadata
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
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
import tornadofx.titledpane
import tornadofx.vbox
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KMutableProperty

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

        bottom = titledpane("Send data") {
            hbox {
                add(buildRadioGroup("Null key", model::keyFile))
                add(buildRadioGroup("Null value", model::valueFile))
                button("Send") {
                    action {
                        fxLaunch(this) {
                            controller.sendRecord(model.keyFile, model.valueFile)
                            onMain {
                                ErrorView.find(this@TopicDetailView, "Message sent").openModal()
                            }
                        }
                    }
                }
            }
        }

    }

    private fun buildRadioGroup(nullText: String, property: KMutableProperty<File?>): VBox {
        return vbox {
            val keyGroup = ToggleGroup()
            val nullRadio = radiobutton(nullText, keyGroup) {
                isSelected = true
                action {
                    model.keyFile = null
                }
            }
            radiobutton("File", keyGroup) {
                action {
                    val chosenFile: File? = FileChooser().apply {
                        this.selectedExtensionFilter = FileChooser.ExtensionFilter("All files", "*.*")
                        this.title = "Chose file"
                    }.showOpenDialog(currentWindow)
                    if (chosenFile != null) {
                        text = chosenFile.absoluteFile.name
                        property.setter.call(chosenFile.absoluteFile)
                    } else {
                        text = "File"
                        property.setter.call(null)
                        nullRadio.isSelected = true
                    }
                }
            }
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
                val mapped = records.map { Record(it.first ?: "[NULL]", it.second ?: "[NULL]") }
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
        var keyFile: File? = null
        var valueFile: File? = null
        var startOnEarliest: Boolean = false
        val recordList: ObservableList<Record> = observableList()
        val status = SimpleObjectProperty<Status>(Status.NotStarted)
        val statusConverter = object : StringConverter<Status>() {
            override fun toString(value: Status): String = value.text

            override fun fromString(string: String?): Status {
                throw UnsupportedOperationException()
            }
        }

        enum class Status(val text: String) {
            NotStarted("Start"), Active("Pause"), Paused("Resume")
        }
    }

    companion object {
        fun create(scope: Scope, topic: Topic, keyDeserializer: DeserializerMetadata, valueDeserializer: DeserializerMetadata) =
                find(
                        TopicDetailView::class,
                        scope.copy(), // todo can be improved to reuse shared parts of the scope
                        TopicDetailController::topic to topic,
                        TopicDetailController::keyMetadata to keyDeserializer,
                        TopicDetailController::valueMetadata to valueDeserializer
                )
    }

    data class Record(val key: String, val value: String) {
        val line = lineCounter.getAndIncrement()

        private companion object {
            val lineCounter = AtomicLong(0)
        }
    }
}