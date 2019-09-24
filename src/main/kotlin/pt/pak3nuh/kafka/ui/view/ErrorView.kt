package pt.pak3nuh.kafka.ui.view

import tornadofx.*

class ErrorView : View("My View") {

    private val errorMsg by param<String>()

    override val root = borderpane {
        center {
            hbox {
                val area = textarea(errorMsg)
                area.isEditable = false
            }
        }
    }

    companion object {
        fun find(parent: Component, errorMsg: String) =
                parent.find<ErrorView>(ErrorView::errorMsg to errorMsg)
    }
}
