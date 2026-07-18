# Harmony-Lift Model Setup Guide

## Pre-requisites

The AI Music Tutor requires a local LLM in GGUF format to function entirely offline. We recommend using `Llama-3.2-1B-Instruct-Q3_K_M.gguf` for its optimal balance of music theory reasoning and low memory footprint on Android.

## Installation Steps

1. **Download the Model**
   Download `Llama-3.2-1B-Instruct-Q3_K_M.gguf` to your development machine.

2. **Push to Device (ADB)**
   Native C++ applications cannot stream efficiently from compressed APK `assets`. You must place the model in the app's external files directory.

   Connect your Android device and run:
   ```bash
   adb push path/to/Llama-3.2-1B-Instruct-Q3_K_M.gguf /sdcard/Android/data/com.harmonylift.app/files/models/
   ```

3. **Verify App Permissions**
   Ensure the Harmony-Lift application has file read permissions, or ensure the file is placed directly in the app-specific directory where permissions are implicitly granted.

4. **Launch App**
   Open Harmony-Lift, play a chord, and tap "Ask Tutor". The model will load lazily into RAM and begin streaming responses.

## Troubleshooting
*   **"Tutor disconnected. Fallback to offline visuals."**: The file might be corrupted, missing, or the device ran out of memory. Check Logcat for "LlamaNative" or "LocalModelManager" exceptions.
