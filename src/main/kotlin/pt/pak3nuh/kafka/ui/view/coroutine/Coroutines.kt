package pt.pak3nuh.kafka.ui.view.coroutine

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.pak3nuh.kafka.ui.log.getSlfLogger

private val logger = getSlfLogger("pt.pak3nuh.kafka.ui.view.coroutine.CoroutinesKt")

fun <T> fxLaunch(
        vararg disableNodes: Node,
        bgAction: suspend () -> T
): Job {
    val scope = CoroutineScope(Dispatchers.Default)
    return scope.launch {
        try {
            logger.debug("Disabling nodes and executing action")
            setEnable(disableNodes, true)
            val value = bgAction()
        } finally {
            logger.debug("Reenabling the nodes")
            setEnable(disableNodes, false)
        }
    }
}

suspend fun continueOnMain(action: suspend () -> Unit) {
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
