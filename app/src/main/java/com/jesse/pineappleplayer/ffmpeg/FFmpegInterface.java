package com.jesse.pineappleplayer.ffmpeg;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class FFmpegInterface {

    private FFmpegInterface() {}

    public static class SingleTon {
        public static final FFmpegInterface INSTANCE = new FFmpegInterface();
    }


    public static FFmpegInterface getInstance() {
        return SingleTon.INSTANCE;
    }

    public native int initH264Decoder();
    public native int decodeh264Data(byte[] srcData, int dataLen, byte[] outData);

}
