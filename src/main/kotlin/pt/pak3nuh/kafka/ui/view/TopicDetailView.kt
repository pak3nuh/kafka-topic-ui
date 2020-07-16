package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.util.StringConverter
import kotlinx.coroutines.delay
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
import tornadofx.checkbox
import tornadofx.enableWhen
import tornadofx.find
import tornadofx.hbox
import tornadofx.label
import tornadofx.observableList
import tornadofx.radiobutton
import tornadofx.readonlyColumn
import tornadofx.style
import tornadofx.tableview
import tornadofx.titledpane
import tornadofx.vbox
import java.io.File
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty

class TopicDetailView : CoroutineView("Topic Detail") {

    private val model: Model = Model()
    private val topic: Topic by param()
    private val controller: TopicDetailController by inject(params = params.plus(sequenceOf(
            TopicDetailController::model.name to model
    )))

    override val root: Parent = borderpane {
        title = "Topic View: ${topic.name}"
        // start pause seek settings assignment
        top = hbox {
            val notStarted = model.status.asBoolean { it == Model.Status.NotStarted }
            spacing = 10.0
            alignment = Pos.CENTER
            val group = ToggleGroup()
            radiobutton("From start", group) {
                enableWhen(notStarted)
                action {
                    model.startOnEarliest = true
                }
            }
            radiobutton("Last offset", group){
                enableWhen(notStarted)
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
            checkbox("Follow", model.followCheck)
        }

        center = tableview(model.recordList) {
            readonlyColumn("Time", Record::time)
            readonlyColumn("Key (${controller.keyMetadata.name})", Record::key) { prefWidth = 200.0 }
            readonlyColumn("Value (${controller.valueMetadata.name})", Record::value) { prefWidth = 200.0 }
            model.recordList.addListener(ListChangeListener {
                if (model.followCheck.value) {
                    this.scrollTo(model.recordList.size - 1)
                }
            })
        }

        bottom = titledpane("Send data") {
            isExpanded = false
            hbox {
                spacing = 20.0
                alignment = Pos.CENTER
                add(buildRadioGroup("Null key", model::keyFile))
                add(buildRadioGroup("Null value", model::valueFile))
                button("Send") {
                    action {
                        fxLaunch(this) {
                            controller.sendRecord(model.keyFile, model.valueFile)
                            onMain {
                                model.messageLabelText.value = "Message sent"
                                delay(2_000)
                                model.messageLabelText.value = ""
                            }
                        }
                    }
                }
                label(model.messageLabelText) {
                    prefWidth = 200.0
                    style { this.textFill = Color.RED }
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
        val followCheck = SimpleBooleanProperty(false)
        val messageLabelText = SimpleStringProperty()

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
        val time: LocalDateTime = LocalDateTime.now()
    }
}