package pt.pak3nuh.kafka.ui.view.coroutine

import javafx.scene.Node
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun launch(
        vararg disableNodes: Node,
        action: suspend () -> Unit
) {
    GlobalScope.launch {
        try {
            setEnable(disableNodes, true)
            action()
        } catch (ex: Exception) {
            setEnable(disableNodes, false)
        }
    }
}

private fun setEnable(nodes: Array<out Node>, disabled: Boolean) {
    nodes.forEach {
        it.isDisable = disabled
    }
}
