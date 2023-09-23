package org.magmaoffenburg.roboviz.util

import java.lang.IllegalStateException

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
