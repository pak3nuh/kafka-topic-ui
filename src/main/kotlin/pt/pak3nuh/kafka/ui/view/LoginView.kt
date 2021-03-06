package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TextFormatter
import pt.pak3nuh.kafka.ui.config.SettingsConfig
import pt.pak3nuh.kafka.ui.controller.LoginController
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Broker
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import pt.pak3nuh.kafka.ui.view.coroutine.onMain
import tornadofx.action
import tornadofx.bind
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.center
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.hbox
import tornadofx.textfield
import java.util.function.UnaryOperator

private val logger = getSlfLogger<LoginView>()

class LoginView : CoroutineView("Login") {

    private val controller: LoginController by inject()
    private val settings: SettingsConfig by di()
    private val viewModel = ViewModel()

    override val root = borderpane {
        center {
            form {
                fieldset {
                    field("Host") {
                        textfield {
                            bind(viewModel.hostText)
                            viewModel.hostText.value = "127.0.0.1"
                        }
                    }
                    field("Port") {
                        textfield {
                            bind(viewModel.hostPort)
                            viewModel.hostPort.value = "9092"
                            textFormatter = TextFormatter<String>(UnaryOperator { change ->
                                if (change.text.matches("[0-9]*".toRegex())) {
                                    change
                                } else {
                                    null
                                }
                            })

                        }
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
                            onMain {
                                if (broker != null) {
                                    val topicView = find<TopicListView>()
                                    settings.configureDefaults(topicView)
                                    this@LoginView.replaceWith(topicView)
                                } else {
                                    ErrorView.find(this@LoginView, "Cannot connect to broker").openModal()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun tryConnect(): Broker? {
        val host: String = viewModel.hostText.value
        val port: String = viewModel.hostPort.value
        logger.info("Connecting to $host:$port")
        return controller.getBroker(host, port)
    }

    private class ViewModel {
        val hostText = SimpleStringProperty()
        val hostPort = SimpleStringProperty()
    }
}