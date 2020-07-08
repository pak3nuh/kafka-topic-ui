package pt.pak3nuh.kafka.ui.app

import tornadofx.FX
import tornadofx.Scope
import tornadofx.ScopedInstance

/**
 * Copies the scope components into another instance.
 *
 * Because components are singletons on each scope, this can create child scopes to have components with
 * multiple instances.
 */
fun Scope.copy(vararg components: ScopedInstance): Scope {
    val original = FX.getComponents(this)
    val values: Array<ScopedInstance> = original.values.toTypedArray()
    return Scope(*values.plus(components))
}