#include <jni.h>
#include <string>
#include <iostream>
#include "tuner/TunerInputEngine.h"
#include "logging_macros.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_createEngine(
        JNIEnv *env,
        jclass clazz) {

    // We use std::nothrow so `new` returns a nullptr if the engine creation fails
    auto *engine = new(std::nothrow) TunerInputEngine();
    if (engine == nullptr) {
        LOGE("Could not instantiate HelloOboeEngine");
        return 0;
    }
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT jboolean JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_setParameters(
        JNIEnv *env,
        jclass clazz,
        jlong engineHandle,
        jfloat buffer_size,
        jfloat min_amplitude,
        jfloat max_frequency) {

    auto *engine = reinterpret_cast<TunerInputEngine *>(engineHandle);
    return engine->setParameters(buffer_size, min_amplitude, max_frequency);
}

JNIEXPORT jint JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_startEngine(
        JNIEnv *env,
        jclass,
        jlong engineHandle,
        jint deviceId,
        jint channels,
        jint sampleRate) {

    auto *engine = reinterpret_cast<TunerInputEngine *>(engineHandle);
    return static_cast<jint>(engine->start(deviceId, channels, sampleRate));
}

JNIEXPORT jint JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_stopEngine(
        JNIEnv *env,
        jclass,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<TunerInputEngine *>(engineHandle);
    return static_cast<jint>(engine->stop());
}

JNIEXPORT void JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_destroyEngine(
        JNIEnv *env,
        jclass clazz,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<TunerInputEngine *>(engineHandle);
    engine->stop();
    delete engine;
}

JNIEXPORT jfloat JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_queryFrequency(
        JNIEnv *env,
        jclass clazz,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<TunerInputEngine *>(engineHandle);
    return static_cast<jfloat>(engine->queryFrequency());
}

JNIEXPORT void JNICALL
Java_software_blob_audio_tuner_engine_TunerInputEngine_getSampleBuffer(
        JNIEnv *env,
        jclass clazz,
        jlong engineHandle,
        jfloatArray buf) {

    auto *engine = reinterpret_cast<TunerInputEngine *>(engineHandle);
    auto *wav = engine->getWav();

    env->SetFloatArrayRegion(buf, 0, wav->numFrames, wav->samples);
}

}