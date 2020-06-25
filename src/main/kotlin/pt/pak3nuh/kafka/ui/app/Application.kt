package pt.pak3nuh.kafka.ui.app

import pt.pak3nuh.kafka.ui.injector.SpringContainer
import pt.pak3nuh.kafka.ui.view.LoginView
import tornadofx.*

class MyApp : App(LoginView::class, Styles::class, Scope())

fun main(args: Array<String>) {
    FX.dicontainer = SpringContainer()
    launch<MyApp>(args)
}

