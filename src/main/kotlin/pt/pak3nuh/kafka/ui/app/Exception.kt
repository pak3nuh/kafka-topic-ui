package pt.pak3nuh.kafka.ui.app

class ApplicationException(ex: Exception) : Exception(ex)

fun <T> wrapEx(block: () -> T): T {
    try {
        return block()
    } catch (ex: Exception) {
        throw ApplicationException(ex)
    }
}