package pt.pak3nuh.kafka.ui.view

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import pt.pak3nuh.kafka.ui.controller.TopicsController
import pt.pak3nuh.kafka.ui.service.Broker
import tornadofx.*

class TopicsFragment(broker: Broker) : Fragment() {

    private val controller by inject<TopicsController>(scope, "broker" to broker)

    override val root = borderpane {
        top = hbox {
            val list = listmenu()
            controller.fillTopics(list)
        }
    }
}

fun <T> asd(data: suspend () -> T, paint: (T) -> Unit) {

    val async = GlobalScope.async { data() }


}