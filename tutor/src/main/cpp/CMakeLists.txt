cmake_minimum_required(VERSION 3.22.1)
project("llama-android")

# ─── Fetch llama.cpp from GitHub ───────────────────────────────────────────
include(FetchContent)

FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY https://github.com/ggerganov/llama.cpp.git
    GIT_TAG        b4170          # Stable release with current API
    GIT_SHALLOW    TRUE           # Shallow clone — much faster download
)

# Disable all extras we don't need for Android
set(LLAMA_BUILD_TESTS     OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES  OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER    OFF CACHE BOOL "" FORCE)
set(GGML_METAL            OFF CACHE BOOL "" FORCE)
set(GGML_CUDA             OFF CACHE BOOL "" FORCE)
set(GGML_OPENCL           OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS     OFF CACHE BOOL "" FORCE)

FetchContent_MakeAvailable(llama_cpp)

# ─── Our JNI shared library ────────────────────────────────────────────────
add_library(llama-android SHARED
    llama_jni.cpp
)

target_compile_features(llama-android PRIVATE cxx_std_17)

target_link_libraries(llama-android
    PRIVATE
    llama
    android
    log
)
