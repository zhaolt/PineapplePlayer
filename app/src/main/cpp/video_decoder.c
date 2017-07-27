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
int initFlag = 0;
typedef struct _VideoDec {
    AVCodecContext *pCodecCtx;
    AVFrame *pFrame;
    AVPacket packet;
    struct SwsContext *pSwsCtx;
    AVCodec *pCodec;

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
