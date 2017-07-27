#include <stdio.h>
#include <jni.h>
#include "video_decoder.h"

jint Java_com_jesse_pineappleplayer_ffmpeg_FFmpegInterface_initH264Decoder(JNIEnv *env, jobject jobj)
{
    int ret = init_avc_dec();
    return ret;
}

jint Java_com_jesse_pineappleplayer_ffmpeg_FFmpegInterface_decodeH264Data(JNIEnv *env,
                                                                           jobject jobj,
                                                                           jbyteArray srcData,
                                                                           jint dataLen,
                                                                           jbyteArray outData)
{
    int ret = 0;
    jbyte *srcBuffer = (*env)->GetByteArrayElements(env, srcData, 0);
    jbyte *outBuffer = (*env)->GetByteArrayElements(env, outData, 0);
    ret = decodeVideoData((uint8_t*) srcBuffer, dataLen, (uint8_t *) outBuffer);
    (*env)->ReleaseByteArrayElements(env, srcData, srcBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, outData, outBuffer, 0);
    return ret;
}
