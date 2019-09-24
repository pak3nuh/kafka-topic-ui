package pt.pak3nuh.kafka.ui.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.Broker
import pt.pak3nuh.kafka.ui.service.BrokerService
import pt.pak3nuh.kafka.ui.view.coroutine.continueOnMain
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import tornadofx.*
import java.util.function.UnaryOperator

private val logger = getSlfLogger<LoginView>()

class LoginView : View("Login") {

    private val brokerService by di<BrokerService>()
    private var hostText: TextField by singleAssign()
    private var hostPort: TextField by singleAssign()

    override val root = borderpane {
        center {
            form {
                fieldset {
                    field("Host") {
                        hostText = textfield()
                        hostText.text = "127.0.0.1"
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
                        fxLaunch(this) {
                            val broker = tryConnect()
                            if (broker != null) {
                                continueOnMain {
                                    this@LoginView.replaceWith(TopicsView.find(this@LoginView, broker))
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
        return if (broker.isAvailable())
            broker
        else {
            continueOnMain {
                ErrorView.find(this, "Cannot connect to broker").openModal()
            }
            null
        }
    }
}