package pt.pak3nuh.kafka.ui.view

import javafx.scene.Parent
import pt.pak3nuh.kafka.ui.controller.TopicDetailController
import pt.pak3nuh.kafka.ui.service.deserializer.Deserializer
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import tornadofx.*

class TopicDetailView: CoroutineView("Topic Detail") {

    private val keyDeserializer: Deserializer by param()
    private val valueDeserializer: Deserializer by param()
    private val controller: TopicDetailController by di()

    override val root: Parent = borderpane {

        // start pause seek settings assignment
        top = hbox {

        }

        center = vbox {
            // keys
            listview<String> {

            }

            // value
            textarea {

            }
        }

    }

}