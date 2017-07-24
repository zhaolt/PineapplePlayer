//
// Created by ZhaoLiangtai on 2017/7/24.
//

#ifndef PINEAPPLEPLAYER_VIDEO_DECODER_H_H
#define PINEAPPLEPLAYER_VIDEO_DECODER_H_H

#include <stdint.h>

int init_dec(int mimeType);
int decodeVideoData(uint8_t* srcData, int dataLen, uint8_t* outData);
#endif //PINEAPPLEPLAYER_VIDEO_DECODER_H_H
