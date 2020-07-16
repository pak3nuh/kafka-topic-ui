package pt.pak3nuh.kafka.ui.config

import org.springframework.stereotype.Component
import tornadofx.View
import java.util.*

@Component
class SettingsConfig {
    val windowWidth = 500.0
    val windowHeight = 500.0
    /**
     * Unique ID per application so that consumer groups don't clash
     */
    // in the future this setting can be used to stop the app and resume on the last commit
    val applicationUUID = UUID.randomUUID().toString()

    fun configureDefaults(view: View) {
        val window = requireNotNull(view.currentWindow)
        window.width = windowWidth
        window.height = windowHeight
    }
}