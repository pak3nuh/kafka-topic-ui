package pt.pak3nuh.kafka.ui.service

import org.junit.jupiter.api.Test

class BrokerServiceTest {

    private val service = BrokerService()

    @Test
    internal fun `test connect`() {
        val broker = service.connect("192.168.99.100", 9092)
        val future = future {
            broker.listTopics()
        }
        val sequence = future.get()
        sequence.count()
    }
}