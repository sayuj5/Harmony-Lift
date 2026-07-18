package com.harmonylift.tutor.data.local

import android.util.Log

/**
 * JNI bindings mapping to the underlying llama.cpp C API.
 * This class assumes that `libllama-android.so` has been compiled via NDK.
 */
object LlamaNative {

    private const val TAG = "HarmonyLiftDebug"

    init {
        // Load the shared library containing the C++ JNI bridge
        try {
            System.loadLibrary("llama-android")
            Log.d(TAG, "[JNI] System.loadLibrary(\"llama-android\") SUCCESS.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "[JNI] UnsatisfiedLinkError: libllama-android.so could not be loaded. ${e.message}")
            // Not re-thrown — LocalModelManager handles the 0L pointer case.
        }
    }

    /**
     * Initializes the Llama model from the given file path.
     * @param modelPath Absolute path to the .gguf file
     * @param contextSize The n_ctx size (e.g., 512 for reduced memory)
     * @param threads Number of CPU threads to use (e.g., 4)
     * @return An opaque pointer (Long) to the llama_context, or 0L if initialization failed (OOM, missing file).
     */
    external fun loadModel(modelPath: String, contextSize: Int, threads: Int): Long

    /**
     * Streams tokens from the model. 
     * Uses a callback to yield tokens back to Kotlin sequentially.
     * @param contextPointer The opaque pointer returned by loadModel
     * @param prompt The formatted input string
     * @param onToken A callback invoked for every generated token string
     * @return true if generation completed successfully, false if cancelled or failed
     */
    external fun streamTokens(
        contextPointer: Long, 
        prompt: String, 
        onToken: (String) -> Unit
    ): Boolean

    /**
     * Frees the allocated memory in C++. 
     * MUST be called to prevent memory leaks when closing the tutor.
     */
    external fun freeModel(contextPointer: Long)
}

