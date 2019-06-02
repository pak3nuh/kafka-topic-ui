package pt.pak3nuh.kafka.ui.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.BrokerService
import pt.pak3nuh.kafka.ui.view.coroutine.launchFx
import pt.pak3nuh.kafka.ui.view.coroutine.continueOnMain
import tornadofx.*
import java.util.function.UnaryOperator

private val logger = getSlfLogger<MainView>()

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
                        hostText.text = "192.168.99.100"
                    }
                    field("Port") {
                        hostPort = textfield()
                        hostPort.textFormatter = TextFormatter<String>(UnaryOperator { change ->
                            if (change.text.matches("[0-9]*".toRegex())) {
                                change
                            } else {
                                null
                            }
                        })
                        hostPort.text = "9092"
                    }
                }
            }
        }
        bottom {
            hbox {
                alignment = Pos.CENTER
                button("Login") {
                    action {
                        launchFx(this) {
                            val broker = tryConnect()
                            if (broker != null) {
                                continueOnMain {
                                    TopicsFragment(broker).openWindow()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun tryConnect(): Broker? {
        val host: String = hostText.text
        val port: String = hostPort.text
        logger.info("Connecting to $host:$port")
        val broker = brokerService.connect(host, port.toInt())
        return if(broker.isAvailable()) broker else null
        // todo error view here
    }
}