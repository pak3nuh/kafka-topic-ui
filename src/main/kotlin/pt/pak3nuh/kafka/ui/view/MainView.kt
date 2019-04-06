package pt.pak3nuh.kafka.ui.view

import pt.pak3nuh.kafka.ui.app.Styles
import tornadofx.View
import tornadofx.addClass
import tornadofx.hbox
import tornadofx.label

class MainView : View("Hello TornadoFX") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}