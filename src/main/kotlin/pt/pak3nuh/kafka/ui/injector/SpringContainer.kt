package pt.pak3nuh.kafka.ui.injector

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import tornadofx.*
import kotlin.reflect.KClass

class SpringContainer : DIContainer {

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

    fun close() {
        context.close()
    }
}