package pt.pak3nuh.kafka.ui.app

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import pt.pak3nuh.kafka.ui.view.MainView
import tornadofx.*
import kotlin.reflect.KClass

class MyApp: App(MainView::class, Styles::class)

fun main(args: Array<String>) {
    FX.dicontainer = SpringContainer()
    launch<MyApp>(args)
}

private class SpringContainer : DIContainer {

    private val context = AnnotationConfigApplicationContext(
            "pt.pak3nuh.kafka.ui.config",
            "pt.pak3nuh.kafka.ui.service"
    )

    override fun <T : Any> getInstance(type: KClass<T>): T {
        return context.getBean(type.java)
    }

    override fun <T : Any> getInstance(type: KClass<T>, name: String): T {
        return context.getBean(name, type.java)
    }
}