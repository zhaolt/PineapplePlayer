#include "video_decoder.h"

jint initH264Decoder(JNIEnv *env, jobject jobj) {
    int ret = init_avc_dec();
    return ret;
}

jint decodeH264Data(JNIEnv *env,
                    jobject jobj,
                    jbyteArray srcData,
                    jint dataLen,
                    jbyteArray outData) {
    int ret = 0;
    jbyte *srcBuffer = (*env)->GetByteArrayElements(env, srcData, 0);
    jbyte *outBuffer = (*env)->GetByteArrayElements(env, outData, 0);
    ret = decodeVideoData((uint8_t *) srcBuffer, dataLen, (uint8_t *) outBuffer);
    (*env)->ReleaseByteArrayElements(env, srcData, srcBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, outData, outBuffer, 0);
    return ret;
}

jint decodeFile(JNIEnv *env, jobject jobj, jstring url) {
    int ret = 0;
    ret = decodeFileOrUrl(env, jobj, url);
    return ret;
}


const JNINativeMethod g_methods[] = {
        "initH264Decoder", "()I", (void *) initH264Decoder,
        "decodeH264Data", "([BI[B)I", (void*) decodeH264Data,
        "decodeFile", "(Ljava/lang/String;)I", (void*) decodeFile
};


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jclass cls = NULL;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    cls = (*env)->FindClass(env, "com/jesse/pineappleplayer/ffmpeg/FFmpegInterface");
    g_cls_FFmpegInterface = (*env)->NewWeakGlobalRef(env, cls);
    (*env)->DeleteLocalRef(env, cls);
    (*env)->RegisterNatives(env, g_cls_FFmpegInterface, g_methods, sizeof(g_methods) / sizeof(g_methods[0]));
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    (*env)->UnregisterNatives(env, g_cls_FFmpegInterface);
    (*env)->DeleteWeakGlobalRef(env, g_cls_FFmpegInterface);
}