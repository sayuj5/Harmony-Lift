# Building Harmony-Lift

This guide explains how to set up the build environment and compile Harmony-Lift for both Debug and Release targets.

## 📋 Requirements

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| Android Studio | Hedgehog 2023.1.1 | Recommended: Ladybug 2024.2+ |
| Android SDK | API 26 (Android 8.0) | Target: API 34 |
| Android NDK | r25c | Required for llama.cpp JNI |
| CMake | 3.22.1 | Install via SDK Manager |
| Java | 21 | Set in `compileOptions` |
| Gradle | 8.x | Wrapper included (`gradlew`) |
| Git | 2.x | For cloning |

---

## 🔧 Environment Setup

### 1. Install Android Studio
Download from [developer.android.com/studio](https://developer.android.com/studio).

### 2. Install SDK Components
Open **SDK Manager** in Android Studio and install:
- Android SDK Platform 34
- Android SDK Build-Tools 34.x
- Android NDK (Side by side) r25c
- CMake 3.22.1

### 3. Clone the Repository
```bash
git clone https://github.com/sayuj5/Harmony-Lift.git
cd Harmony-Lift
```

### 4. Open in Android Studio
File → Open → Select the `Harmony-Lift` folder.

Allow Gradle sync to complete.

---

## 🐛 Debug Build

```bash
./gradlew assembleDebug
```

Output APK location:
```
app/build/outputs/apk/debug/app-debug.apk
```

Install directly on a connected device:
```bash
./gradlew installDebug
```

Or via ADB:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🚀 Release Build

### Step 1: Create a Keystore

```bash
keytool -genkey -v -keystore harmonylift-release.jks \
  -alias harmonylift -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Configure Signing in `build.gradle.kts`

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("harmonylift-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "harmonylift"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### Step 3: Build Release

```bash
export KEYSTORE_PASSWORD=your_password
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

Output:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 🛡 ProGuard / R8

The `proguard-rules.pro` file preserves JNI symbols required by llama.cpp:

```proguard
-keep class com.harmonylift.tutor.data.local.LlamaNative {
    native <methods>;
}
```

R8 is enabled for Release builds with resource shrinking. The `noCompress += "gguf"` setting ensures GGUF model files are not broken by APK compression if bundled as assets.

---

## 🧪 Running Tests

### Unit Tests
```bash
./gradlew test
```

### Android Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Lint
```bash
./gradlew lint
```

---

## 🔨 Troubleshooting

### NDK not found
Ensure NDK is installed via SDK Manager and the path is set in `local.properties`:
```properties
ndk.dir=/path/to/Android/Sdk/ndk/25.x.x
```

### Gradle sync fails
```bash
./gradlew --stop
./gradlew build --refresh-dependencies
```

### Build fails with EOCD error
A GGUF model file may have been accidentally placed in `assets/`. Remove it and clean:
```bash
./gradlew clean assembleDebug
```

### `UnsatisfiedLinkError` at runtime
The native `libllama-android.so` is not included in this repository (it must be compiled from llama.cpp source). See [MODEL_SETUP.md](./MODEL_SETUP.md) for details.
