package pt.pak3nuh.kafka.ui.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import pt.pak3nuh.kafka.ui.app.ApplicationException
import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.BrokerService
import tornadofx.*

class MainView : View("Login") {

    private val brokerService by di<BrokerService>()
    private var hostText: TextField by singleAssign()
    private var hostPort: TextField by singleAssign()

    override val root = borderpane {
        center {
            form {
                fieldset {
                    field("Host") {
                        hostText = textfield()
                    }
                    field("Port") {
                        hostPort = textfield()
                    }
                }
            }
        }
        bottom {
            hbox {
                alignment = Pos.CENTER
                button("Login") {
                    action {
                        val broker = tryConnect()
                        if (broker != null) {
                            TopicsFragment(broker).openWindow()
                        }
                    }
                }
            }
        }
    }

    private fun tryConnect(): Broker? {
        return try {
            val host: String = hostText.text
            val port: String = hostPort.text
            brokerService.connect(host, port.toInt())
        } catch (ex: ApplicationException) {
            // todo error view
            null
        }
    }
}