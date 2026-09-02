#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"

#define LOG_TAG "HarmonyLiftDebug"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Holds all native resources for one loaded model session
struct LlamaContext {
    llama_model*   model;
    llama_context* ctx;
    llama_sampler* sampler;
};

extern "C" {

// ─── loadModel ─────────────────────────────────────────────────────────────
// Maps to: LlamaNative.loadModel(modelPath: String, contextSize: Int, threads: Int): Long
JNIEXPORT jlong JNICALL
Java_com_harmonylift_tutor_data_local_LlamaNative_loadModel(
        JNIEnv* env, jobject /*thiz*/,
        jstring modelPath, jint contextSize, jint threads) {

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("[JNI] loadModel() path=%s nCtx=%d threads=%d", path, contextSize, threads);

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;   // CPU-only on Android

    llama_model* model = llama_load_model_from_file(path, model_params);
    env->ReleaseStringUTFChars(modelPath, path);

    if (!model) {
        LOGE("[JNI] llama_load_model_from_file() returned null — bad path or OOM");
        return 0L;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx          = static_cast<uint32_t>(contextSize);
    ctx_params.n_threads      = static_cast<int32_t>(threads);
    ctx_params.n_threads_batch= static_cast<int32_t>(threads);

    llama_context* ctx = llama_new_context_with_model(model, ctx_params);
    if (!ctx) {
        LOGE("[JNI] llama_new_context_with_model() returned null");
        llama_free_model(model);
        return 0L;
    }

    // Build a simple sampler chain (top-k + top-p + temperature + random)
    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    auto* llamaCtx = new LlamaContext{model, ctx, sampler};
    LOGI("[JNI] loadModel() success. pointer=%p", (void*)llamaCtx);
    return reinterpret_cast<jlong>(llamaCtx);
}

// ─── streamTokens ──────────────────────────────────────────────────────────
// Maps to: LlamaNative.streamTokens(contextPointer: Long, prompt: String, onToken: (String)->Unit): Boolean
JNIEXPORT jboolean JNICALL
Java_com_harmonylift_tutor_data_local_LlamaNative_streamTokens(
        JNIEnv* env, jobject /*thiz*/,
        jlong contextPointer, jstring prompt, jobject onToken) {

    auto* llamaCtx = reinterpret_cast<LlamaContext*>(contextPointer);
    if (!llamaCtx) {
        LOGE("[JNI] streamTokens() called with null context pointer");
        return JNI_FALSE;
    }

    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    LOGI("[JNI] streamTokens() prompt len=%zu", strlen(promptStr));

    // Resolve the Kotlin lambda's invoke method
    jclass  cbClass  = env->GetObjectClass(onToken);
    jmethodID invoke = env->GetMethodID(cbClass, "invoke",
                                        "(Ljava/lang/Object;)Ljava/lang/Object;");

    // Tokenize the prompt
    const int          n_ctx   = static_cast<int>(llama_n_ctx(llamaCtx->ctx));
    std::vector<llama_token> tokens(n_ctx);

    int n_tokens = llama_tokenize(
            llamaCtx->model, promptStr, static_cast<int32_t>(strlen(promptStr)),
            tokens.data(), static_cast<int32_t>(tokens.size()), true, true);
    env->ReleaseStringUTFChars(prompt, promptStr);

    if (n_tokens < 0) {
        LOGE("[JNI] Tokenization failed (n_tokens=%d). Prompt may be too long.", n_tokens);
        return JNI_FALSE;
    }
    tokens.resize(static_cast<size_t>(n_tokens));

    // Prefill — process the prompt
    llama_kv_cache_clear(llamaCtx->ctx);
    llama_batch batch = llama_batch_get_one(tokens.data(), static_cast<int32_t>(tokens.size()));
    if (llama_decode(llamaCtx->ctx, batch) != 0) {
        LOGE("[JNI] llama_decode() failed during prompt prefill");
        return JNI_FALSE;
    }

    // Generation loop — max 512 new tokens
    const int max_new_tokens = 512;
    for (int i = 0; i < max_new_tokens; i++) {
        llama_token new_token = llama_sampler_sample(llamaCtx->sampler, llamaCtx->ctx, -1);

        if (llama_token_is_eog(llamaCtx->model, new_token)) {
            LOGI("[JNI] EOG token after %d generated tokens", i);
            break;
        }

        // Detokenize one piece
        char buf[256];
        int  len = llama_token_to_piece(llamaCtx->model, new_token, buf, sizeof(buf) - 1, 0, true);
        if (len > 0) {
            buf[len] = '\0';
            jstring tokenStr = env->NewStringUTF(buf);
            env->CallObjectMethod(onToken, invoke, tokenStr);
            env->DeleteLocalRef(tokenStr);

            if (env->ExceptionCheck()) {
                LOGE("[JNI] Kotlin callback threw an exception — stopping generation");
                env->ExceptionClear();
                break;
            }
        }

        // Feed token back for next step
        llama_batch next_batch = llama_batch_get_one(&new_token, 1);
        if (llama_decode(llamaCtx->ctx, next_batch) != 0) {
            LOGE("[JNI] llama_decode() failed at generation step %d", i);
            break;
        }
    }

    llama_sampler_reset(llamaCtx->sampler);
    return JNI_TRUE;
}

// ─── freeModel ─────────────────────────────────────────────────────────────
// Maps to: LlamaNative.freeModel(contextPointer: Long)
JNIEXPORT void JNICALL
Java_com_harmonylift_tutor_data_local_LlamaNative_freeModel(
        JNIEnv* /*env*/, jobject /*thiz*/, jlong contextPointer) {

    auto* llamaCtx = reinterpret_cast<LlamaContext*>(contextPointer);
    if (!llamaCtx) return;

    LOGI("[JNI] freeModel() pointer=%p", (void*)llamaCtx);
    llama_sampler_free(llamaCtx->sampler);
    llama_free(llamaCtx->ctx);
    llama_free_model(llamaCtx->model);
    delete llamaCtx;
    LOGI("[JNI] freeModel() complete");
}

} // extern "C"
