package pt.pak3nuh.kafka.ui.view.coroutine

import javafx.scene.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.pak3nuh.kafka.ui.log.getSlfLogger

private val logger = getSlfLogger("pt.pak3nuh.kafka.ui.view.coroutine.CoroutinesKt")

fun launchFx(
        vararg disableNodes: Node,
        action: suspend () -> Unit
) {
    GlobalScope.launch {
        try {
            logger.debug("Disabling nodes and executing action")
            setEnable(disableNodes, true)
            action()
        } finally {
            logger.debug("Reenabling the nodes")
            setEnable(disableNodes, false)
        }
    }
}

suspend fun continueOnMain(action: () -> Unit) {
    withContext(Dispatchers.Main) {
        action()
    }
}


private suspend fun setEnable(nodes: Array<out Node>, disabled: Boolean) {
    continueOnMain {
        nodes.forEach {
            it.isDisable = disabled
        }
    }
}
