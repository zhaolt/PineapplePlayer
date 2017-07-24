package com.jesse.pineappleplayer.ffmpeg;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class FFmpegInterface {

    private FFmpegInterface() {}

    public static class SingleTon {
        public static final FFmpegInterface INSTANCE = new FFmpegInterface();
    }

    public FFmpegInterface getInstance() {
        return SingleTon.INSTANCE;
    }

    public native int initVideoDecoder();
    public native int decodeVideoData(byte[] srcData, byte[] outData);

}
