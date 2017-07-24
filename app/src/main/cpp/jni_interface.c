#include <stdio.h>
#include <jni.h>
#include "video_decoder.h"

jint Java_com_jesse_pineappleplayer_ffmpeg_FFmpegInterface_initVideoDecoder(JNIEnv *env, jobject jobj, jint mimeType)
{
    int ret = init_dec(mimeType);
    return ret;
}

jint Java_com_jesse_pineappleplayer_ffmpeg_FFmpegInterface_decodeVideoData(JNIEnv *env,
                                                                           jobject jobj,
                                                                           jbyteArray srcData,
                                                                           jint dataLen,
                                                                           jbyteArray outData)
{
    int ret = 0;
    jbyte *srcBuffer = (jbyte*)(*env)->GetByteArrayElements(env, srcData, 0);
    jbyte *outBuffer = (jbyte*)(*env)->GetByteArrayElements(env, outData, 0);
    decodeVideoData((uint8_t*) srcBuffer, dataLen, (uint8_t *) outBuffer);
    (*env)->ReleaseByteArrayElements(env, srcData, srcBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, outData, outBuffer, 0);
    return ret;
}
