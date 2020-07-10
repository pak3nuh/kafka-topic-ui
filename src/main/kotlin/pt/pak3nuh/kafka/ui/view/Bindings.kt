package pt.pak3nuh.kafka.ui.view

import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty

fun <T> Property<T>.asBoolean(pred: (T?) -> Boolean): BooleanProperty {
    val property = SimpleBooleanProperty(pred(this.value))
    this.addListener { _, _, new: T? ->
        property.set(pred(new))
    }
    return property
}