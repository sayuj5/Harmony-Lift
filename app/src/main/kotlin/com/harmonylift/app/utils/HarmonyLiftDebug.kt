package com.harmonylift.app.utils

import android.util.Log

object HarmonyLiftDebug {
    private const val TAG = "HarmonyLiftDebug"
    @PublishedApi internal var isEnabled = true // Set to false in release builds if desired

    fun d(message: String) {
        if (isEnabled) Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }

    fun i(message: String) {
        if (isEnabled) Log.i(TAG, message)
    }

    fun w(message: String) {
        if (isEnabled) Log.w(TAG, message)
    }

    inline fun <T> measureTime(label: String, block: () -> T): T {
        if (!isEnabled) return block()
        val start = System.currentTimeMillis()
        val result = block()
        val end = System.currentTimeMillis()
        d("[$label] executed in ${end - start} ms")
        return result
    }
}
