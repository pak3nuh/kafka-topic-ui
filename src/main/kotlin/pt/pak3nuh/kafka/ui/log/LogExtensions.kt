package pt.pak3nuh.kafka.ui.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T : Any> getSlfLogger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

fun getSlfLogger(name: String): Logger {
    return LoggerFactory.getLogger(name)
}