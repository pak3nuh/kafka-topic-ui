package pt.pak3nuh.kafka.ui.app

import pt.pak3nuh.kafka.ui.view.MainView
import tornadofx.App
import tornadofx.launch

class MyApp: App(MainView::class, Styles::class)

fun main(args: Array<String>) {
    launch<MyApp>(args)
}