//
// Created by ZhaoLiangtai on 2017/7/24.
//
#include <string.h>
#include <stdio.h>
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"

#ifdef ANDROID

#include <jni.h>
#include <android/log.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("(^_^) " format "\n", ##__VA_ARGS__)
#endif
void sendData2Java(uint8_t* deData, int dataLen, JNIEnv *env, jobject jobj);
void updateSize2Java(int width, int height, JNIEnv *env, jobject jobj);
int initFlag = 0;
typedef struct _VideoDec {
    AVCodecContext *pCodecCtx;
    AVFrame *pFrame, *pFrameYUV;
    AVPacket packet;
    struct SwsContext *pSwsCtx;
    AVCodec *pCodec;
    AVFormatContext *pFormatCtx;
} VideoDec;
VideoDec *d = NULL;

int init_avc_dec()
{
    if (initFlag == 1)
    {
        sws_freeContext(d->pSwsCtx);
        av_free_packet(&(d->packet));
        av_free(d->pFrame);
        avcodec_close(d->pCodecCtx);
        av_free(d->pCodecCtx);
    }
    d = (VideoDec*) av_mallocz(sizeof(VideoDec));
    d->pCodec = NULL;
    d->pSwsCtx = NULL;
    avcodec_register_all();
    d->pFrame = av_frame_alloc();
    av_init_packet(&(d->packet));
    d->pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (NULL != d->pCodec)
    {
        d->pCodecCtx = avcodec_alloc_context3(d->pCodec);
        d->pCodecCtx->flags |= CODEC_FLAG_LOW_DELAY;

        if (avcodec_open2(d->pCodecCtx, d->pCodec, NULL) >= 0)
        {
            initFlag = 1;
            return 1;
        }
    }
    else
    {
        return 0;
    }

}

void pgm_save2(unsigned char *buf, int wrap, int xsize, int ysize, uint8_t *pDataOut) {
    int i;
    for (i = 0; i < ysize; i++) {
        memcpy(pDataOut + i * xsize, buf + i * wrap, xsize);
    }
}

int decodeVideoData(uint8_t* srcData, int dataLen, uint8_t* outData)
{
    int ret = 0;
    int got_picture;
    if (!d)
    {
        return -1;
    }
    d->packet.data = srcData;
    d->packet.size = dataLen;
    ret = avcodec_decode_video2(d->pCodecCtx, d->pFrame, &got_picture, &(d->packet));
    if (got_picture)
    {
        pgm_save2(d->pFrame->data[0],
                  d->pFrame->linesize[0], d->pFrame->width, d->pFrame->height, outData);
        pgm_save2(d->pFrame->data[1],
                  d->pFrame->linesize[1],
                  d->pFrame->width/2,
                  d->pFrame->height/2,
                  outData + d->pFrame->width * d->pFrame->height);
        pgm_save2(d->pFrame->data[2],
                  d->pFrame->linesize[2],
                  d->pFrame->width/2,
                  d->pFrame->height/2,
                  outData + d->pFrame->width * d->pFrame->height * 5 / 4);
    }
    else
    {
        return -1;
    }
    return ret;
}


int decodeFile(JNIEnv* env, jobject jobj, jstring url)
{
    int i, videoIndex;
    int ret, got_picture;
    int frame_cnt;

    char input_str[500] = {0};
    char info[500] = {0};
    sprintf(input_str,"%s",(*env)->GetStringUTFChars(env, url, NULL));

    av_register_all();
    avformat_network_init();
    VideoDec *pDec = (VideoDec*) av_mallocz(sizeof(VideoDec));
    pDec->pFormatCtx = avformat_alloc_context();

    if (avformat_open_input(&(pDec->pFormatCtx), input_str
                            /*"rtmp://live.hkstv.hk.lxdns.com/live/hks"*/, NULL, NULL) != 0)
    {
        LOGE("Couldn't open input stream.\n");
        return -1;
    }

    if (avformat_find_stream_info(pDec->pFormatCtx, NULL) < 0)
    {
        LOGE("Couldn't find stream information\n");
        return -1;
    }

    videoIndex = -1;
    for (i = 0; i < pDec->pFormatCtx->nb_streams; i++) {
        if (pDec->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO)
        {
            videoIndex = i;
            break;
        }
    }
    if (videoIndex == -1)
    {
        LOGE("Couldn't find a video stream.\n");
        return -1;
    }
    pDec->pCodecCtx = pDec->pFormatCtx->streams[videoIndex]->codec;
    pDec->pCodec = avcodec_find_decoder(pDec->pCodecCtx->codec_id);
    if (pDec->pCodec == NULL)
    {
        LOGE("Couldn't find Codec.\n");
        return -1;
    }
    if (avcodec_open2(pDec->pCodecCtx, pDec->pCodec, NULL) < 0)
    {
        LOGE("Couldn't open codec.\n");
        return -1;
    }

    // int size = w * h + (w * h / 2); yuv420
    int dataLen = pDec->pCodecCtx->width * pDec->pCodecCtx->height +
                  (pDec->pCodecCtx->width * pDec->pCodecCtx->height / 2);
    updateSize2Java(pDec->pCodecCtx->width, pDec->pCodecCtx->height, env, jobj);
    uint8_t* deData = malloc(dataLen);
    pDec->pFrame = av_frame_alloc();
    pDec->pFrameYUV = av_frame_alloc();
    av_image_fill_arrays(pDec->pFrameYUV->data, pDec->pFrameYUV->linesize, deData,
                         AV_PIX_FMT_YUV420P, pDec->pCodecCtx->width, pDec->pCodecCtx->height, 1);

    av_init_packet(&(pDec->packet));

    pDec->pSwsCtx = sws_getContext(pDec->pCodecCtx->width, pDec->pCodecCtx->height,
                                   pDec->pCodecCtx->pix_fmt, pDec->pCodecCtx->width,
                                   pDec->pCodecCtx->height, AV_PIX_FMT_YUV420P, SWS_BICUBIC,
                                   NULL, NULL, NULL);

    sprintf(info,   "[Input     ]%s\n", input_str);
    sprintf(info, "%s[Format    ]%s\n",info, pDec->pFormatCtx->iformat->name);
    sprintf(info, "%s[Codec     ]%s\n",info, pDec->pCodecCtx->codec->name);
    sprintf(info, "%s[Resolution]%dx%d\n",info, pDec->pCodecCtx->width, pDec->pCodecCtx->height);

    frame_cnt = 0;

    while (av_read_frame(pDec->pFormatCtx, &(pDec->packet)) >= 0)
    {
        if (pDec->packet.stream_index == videoIndex)
        {
            ret = avcodec_decode_video2(pDec->pCodecCtx, pDec->pFrame, &got_picture, &(pDec->packet));
            if (ret < 0)
            {
                LOGE("Decode Error.\n");
                return -1;
            }
            if (got_picture)
            {
                sws_scale(pDec->pSwsCtx, (const uint8_t* const*) pDec->pFrame->data,
                          pDec->pFrame->linesize, 0, pDec->pCodecCtx->height, pDec->pFrameYUV->data,
                          pDec->pFrameYUV->linesize);
                char pictype_str[10]={0};
                pgm_save2(pDec->pFrame->data[0],
                          pDec->pFrame->linesize[0], pDec->pFrame->width, pDec->pFrame->height,
                          deData);
                pgm_save2(pDec->pFrame->data[1],
                          pDec->pFrame->linesize[1],
                          pDec->pFrame->width/2,
                          pDec->pFrame->height/2,
                          deData + pDec->pFrame->width * pDec->pFrame->height);
                pgm_save2(pDec->pFrame->data[2],
                          pDec->pFrame->linesize[2],
                          pDec->pFrame->width/2,
                          pDec->pFrame->height/2,
                          deData + pDec->pFrame->width * pDec->pFrame->height * 5 / 4);

                switch(pDec->pFrame->pict_type){
                    case AV_PICTURE_TYPE_I:sprintf(pictype_str,"I");break;
                    case AV_PICTURE_TYPE_P:sprintf(pictype_str,"P");break;
                    case AV_PICTURE_TYPE_B:sprintf(pictype_str,"B");break;
                    default:sprintf(pictype_str,"Other");break;
                }
                LOGI("Frame Index: %5d. Type:%s",frame_cnt,pictype_str);
                sendData2Java(deData, dataLen, env, jobj);
                frame_cnt++;
            }
        }
    }
    return 2;
}

void sendData2Java(uint8_t* deData, int dataLen, JNIEnv *env, jobject jobj)
{
    jclass jclazz = (*env)->GetObjectClass(env, jobj);
    jmethodID jmethodid = (*env)->GetMethodID(env, jclazz, "sendData2Java", "([B)V");
    jbyte* jbyteDeData = (jbyte*) deData;
    jbyteArray outData = (*env)->NewByteArray(env, dataLen);
    (*env)->SetByteArrayRegion(env, outData, 0, dataLen, jbyteDeData);
    (*env)->CallVoidMethod(env, jobj, jmethodid, outData);
    (*env)->DeleteLocalRef(env, outData);
}

void updateSize2Java(int width, int height, JNIEnv *env, jobject jobj)
{
    jclass  jclazz = (*env)->GetObjectClass(env, jobj);
    jmethodID  jmethodid = (*env)->GetMethodID(env, jclazz, "updateSize", "(II)V");
    (*env)->CallVoidMethod(env, jobj, jmethodid, width, height);
}
