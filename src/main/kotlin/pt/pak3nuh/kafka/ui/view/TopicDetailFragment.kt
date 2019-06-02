package pt.pak3nuh.kafka.ui.view

import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.Topic
import tornadofx.*

class TopicDetailFragment : Fragment("Topic Detail") {

    private val topic by param<Topic>()
    private val broker by param<Broker>()


    override val root = borderpane {
        top {

        }
    }


    companion object {
        fun find(parent: Component, broker: Broker, topic: Topic) =
                parent.find<TopicDetailFragment>(
                        "broker" to broker,
                        "topic" to topic
                )
    }
}
