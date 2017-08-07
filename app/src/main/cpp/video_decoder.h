//
// Created by ZhaoLiangtai on 2017/7/24.
//

#ifdef ANDROID

#include <jni.h>
#include <android/log.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("(^_^) " format "\n", ##__VA_ARGS__)
#endif

#ifndef PINEAPPLEPLAYER_VIDEO_DECODER_H_H
#define PINEAPPLEPLAYER_VIDEO_DECODER_H_H

#include <stdint.h>
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"

int init_avc_dec();
int decodeVideoData(uint8_t* srcData, int dataLen, uint8_t* outData);
int decodeFileOrUrl(JNIEnv* env, jobject jobj, jstring url);
static jclass g_cls_FFmpegInterface = NULL;
#endif //PINEAPPLEPLAYER_VIDEO_DECODER_H_H
