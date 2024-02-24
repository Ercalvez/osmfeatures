package de.westnordost.osmfeatures

import kotlin.test.assertTrue

object TestUtils {
    fun <T> assertEqualsIgnoreOrder(a: Collection<T>, b: Collection<T>) {
        assertTrue(a.size == b.size && a.containsAll(b))
    }
}