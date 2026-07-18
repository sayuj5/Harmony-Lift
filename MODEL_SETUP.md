# Model Setup Guide

This guide explains everything you need to know about the Llama 3.2 AI model used by Harmony-Lift.

## 📋 Table of Contents

- [Why GGUF?](#why-gguf)
- [Model Details](#model-details)
- [Storage Requirements](#storage-requirements)
- [Automatic Download](#automatic-download)
- [Manual Installation](#manual-installation)
- [Expected File Location](#expected-file-location)
- [Common Issues](#common-issues)
- [Troubleshooting](#troubleshooting)

---

## Why GGUF?

**GGUF** (GPT-Generated Unified Format) is the state-of-the-art format for quantized large language models. Harmony-Lift uses GGUF because:

| Reason | Explanation |
|--------|-------------|
| 🚀 **Performance** | Optimized for CPU inference — no GPU required |
| 📦 **Small Size** | Q3_K_M quantization reduces ~7B parameter quality to ~600MB |
| 🔒 **Privacy** | Entire inference runs on-device, no cloud needed |
| 🔄 **Compatibility** | Universally supported by llama.cpp on ARM64 Android |
| ⚡ **Speed** | First token in ~2-4 seconds on mid-range Android devices |

---

## Model Details

| Property | Value |
|----------|-------|
| **Model Family** | Llama 3.2 (Meta AI) |
| **Parameters** | 1 Billion |
| **Quantization** | Q3_K_M |
| **Format** | GGUF |
| **Filename** | `Llama-3.2-1B-Instruct-Q3_K_M.gguf` |
| **File Size** | ~600 MB |
| **Source** | [bartowski/Llama-3.2-1B-Instruct-GGUF](https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF) |
| **Context Length** | 512 tokens (configured in `LocalModelManager`) |
| **Thread Count** | 4 (configured in `LocalModelManager`) |

---

## Storage Requirements

Before the model can be downloaded:

| Requirement | Amount |
|------------|--------|
| Free internal storage (filesDir) | **≥ 1 GB** |
| RAM during inference | **~400-500 MB** |
| Temporary download space | **~700 MB** (temp + final) |

> ⚠️ The app checks for 1GB free space before starting any download. If space is insufficient, an error is shown.

---

## Automatic Download

On first launch, if no model is found in `filesDir/models/`, the app will:

1. Display the **Model Download Screen** with a progress indicator.
2. Enqueue a **WorkManager** background task to download the model.
3. Show a persistent **foreground notification** with download progress and a Cancel option.
4. After download, perform a **GGUF header validation** (magic byte check).
5. Transition to **Ready** state and load the model into memory via JNI.

The download is **resumable** — if interrupted, it continues from where it left off using HTTP Range headers.

**Download Source:**
```
https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q3_K_M.gguf
```

---

## Manual Installation

If you prefer to install the model manually (e.g., on a device without internet access):

### Option 1: ADB Push

1. Download the model on your computer:
   ```bash
   # Using wget or browser
   wget "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q3_K_M.gguf"
   ```

2. Push to the device:
   ```bash
   adb shell mkdir -p /data/data/com.harmonylift.app/files/models
   adb push Llama-3.2-1B-Instruct-Q3_K_M.gguf /data/data/com.harmonylift.app/files/models/
   ```

3. Launch the app — it will detect the model and transition to **Ready** state immediately.

### Option 2: Asset Bundle (Development Only)

For development, you can place the GGUF inside `app/src/main/assets/models/`. The app will automatically copy it to `filesDir/models/` on first run.

> ⚠️ **Never commit the GGUF file to Git.** It is listed in `.gitignore`. Asset bundling is only for local development convenience.

---

## Expected File Location

The app **always** loads the model from:

```
/data/data/com.harmonylift.app/files/models/Llama-3.2-1B-Instruct-Q3_K_M.gguf
```

In code, this is:
```kotlin
File(context.filesDir, "models/Llama-3.2-1B-Instruct-Q3_K_M.gguf")
```

**Never** use `context.assets`, `getExternalFilesDir()`, or build output paths for model loading.

---

## Common Issues

| Issue | Cause | Solution |
|-------|-------|---------|
| App stuck on "Downloading..." | Slow network or interrupted | Wait or use manual ADB install |
| "Insufficient storage space" | Less than 1GB free on device | Free up internal storage |
| "Invalid GGUF header" | Corrupted or partial download | Delete the `.tmp` file and retry |
| `UnsatisfiedLinkError` | `libllama-android.so` missing | Compile llama.cpp for Android ARM64 |
| Model loads but no output | nCtx too small or OOM | Reduce `nCtx` in `LocalModelManager.kt` |

---

## Troubleshooting

### Check if model is present via ADB
```bash
adb shell ls -lh /data/data/com.harmonylift.app/files/models/
```

Expected output:
```
-rw------- 1 u0_a... u0_a... 598M Llama-3.2-1B-Instruct-Q3_K_M.gguf
```

### Verify GGUF header
```bash
adb shell od -An -tx1 -N4 /data/data/com.harmonylift.app/files/models/Llama-3.2-1B-Instruct-Q3_K_M.gguf
```

Expected: `47 47 55 46` (ASCII: `GGUF`)

### Delete and re-download
```bash
adb shell rm /data/data/com.harmonylift.app/files/models/Llama-3.2-1B-Instruct-Q3_K_M.gguf
adb shell rm /data/data/com.harmonylift.app/files/models/Llama-3.2-1B-Instruct-Q3_K_M.gguf.tmp
```

Then relaunch the app to trigger a fresh download.
