package pt.pak3nuh.kafka.ui.view.coroutine

import javafx.scene.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.pak3nuh.kafka.ui.log.getSlfLogger

private val logger = getSlfLogger("pt.pak3nuh.kafka.ui.view.coroutine.CoroutinesKt")

fun CoroutineView.fxLaunch(
    vararg disableNodes: Node,
    bgAction: suspend () -> Unit
): Job {
    return launch {
        try {
            logger.debug("Disabling nodes and executing action")
            setDisable(disableNodes, true)
            bgAction()
        } finally {
            logger.debug("Reenabling the nodes")
            setDisable(disableNodes, false)
        }
    }
}

suspend inline fun onMain(crossinline fgAction: suspend () -> Unit) {
    withContext(Dispatchers.Main) {
        fgAction()
    }
}


private suspend fun setDisable(nodes: Array<out Node>, disabled: Boolean) {
    onMain {
        nodes.forEach {
            it.isDisable = disabled
        }
    }
}
