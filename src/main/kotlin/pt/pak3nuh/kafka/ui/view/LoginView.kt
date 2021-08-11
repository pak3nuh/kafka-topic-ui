package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TextFormatter
import pt.pak3nuh.kafka.ui.config.SettingsConfig
import pt.pak3nuh.kafka.ui.controller.LoginController
import pt.pak3nuh.kafka.ui.log.getSlfLogger
import pt.pak3nuh.kafka.ui.service.broker.Broker
import pt.pak3nuh.kafka.ui.service.broker.SecurityCredentials
import pt.pak3nuh.kafka.ui.view.coroutine.CoroutineView
import pt.pak3nuh.kafka.ui.view.coroutine.fxLaunch
import pt.pak3nuh.kafka.ui.view.coroutine.onMain
import tornadofx.*
import java.io.File
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
                    field("Security") {
                        choicebox(values = listOf(SecurityType.PLAINTEXT, SecurityType.SSL)) {
                            bind(viewModel.security)
                        }
                    }
                    field("Truststore") {
                        button("Pick file") {
                            action {
                                chooseFile("Pick certificate", emptyArray(), mode = FileChooserMode.Single)
                                        .firstOrNull()
                                        ?.let { file ->
                                            viewModel.truststoreFile = file
                                            text = file.name
                                        }
                            }
                        }
                        // todo mask
                        label("Password")
                        textfield {
                            bind(viewModel.truststorePassword)
                        }
                    }
                    field("Keystore") {
                        button("Pick file") {
                            action {
                                chooseFile("Pick certificate", emptyArray(), mode = FileChooserMode.Single)
                                        .firstOrNull()
                                        ?.let { file ->
                                            viewModel.keystoreFile = file
                                            text = file.name
                                        }
                            }
                        }
                        // todo mask
                        label("Password")
                        textfield {
                            bind(viewModel.keystorePassword)
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
        return when (requireNotNull(viewModel.security.value)) {
            SecurityType.PLAINTEXT -> controller.getBroker(host, port)
            SecurityType.SSL -> {
                val credentials = SecurityCredentials(
                        requireNotNull(viewModel.truststoreFile),
                        requireNotNull(viewModel.keystoreFile),
                        viewModel.truststorePassword.value ?: "",
                        viewModel.keystorePassword.value ?: "",
                        // todo expose
                        "JKS",
                        "JKS",
                )
                controller.getBrokerSsl(host, port, credentials)
            }
        }
    }

    private class ViewModel {
        val hostText = SimpleStringProperty()
        val hostPort = SimpleStringProperty()
        val security = SimpleObjectProperty(SecurityType.PLAINTEXT)
        var truststoreFile: File? = null
        var keystoreFile: File? = null
        val truststorePassword = SimpleStringProperty()
        val keystorePassword = SimpleStringProperty()
    }

    private enum class SecurityType {
        PLAINTEXT, SSL
    }
}