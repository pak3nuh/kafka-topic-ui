package pt.pak3nuh.kafka.ui.service

object AvailableSerdes {

    fun getAllSerdes() {

    }

}

interface SerdeProvider {

    val name: String

    fun createMessageConsumer(): MessageConsumer

}