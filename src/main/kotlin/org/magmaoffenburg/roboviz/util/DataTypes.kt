package org.magmaoffenburg.roboviz.util

enum class Mode {
    LOG,
    LIVE
}

enum class ConfirmResult {
    YES, NO, CANCEL;

    companion object {
        fun fromInt(result: Int) =
            when (result) {
                0 -> YES
                1 -> NO
                2 -> CANCEL
                else -> throw IllegalStateException()
            }
    }
}

class DeferredMethodCall(private val f: () -> Unit, var callRequested: Boolean = false) {
    fun update() {
        if (!callRequested) {
            return
        }
        f()
        callRequested = false
    }
}
