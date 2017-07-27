package com.jesse.pineappleplayer;

import android.app.Application;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class App extends Application {

    static {
        System.loadLibrary("jni_interface");
        System.loadLibrary("ffmpeg");
    }

    public static boolean isSupportMediaCodecDecode = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isSupportMediaCodecDecode = isSupportMediaCodecDecode();
    }

    private boolean isSupportMediaCodecDecode() {
        boolean isSupportVideoDecode = false, isSupportAudioDecode = false;
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo mediaCodecInfo = null;
        for (int i = 0; i < numCodecs && mediaCodecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            for (String type : types) {
                if (type.contains("video/") && !isSupportVideoDecode) {
                    isSupportVideoDecode = true;
                }
                if (type.contains("audio/") && !isSupportAudioDecode) {
                    isSupportAudioDecode = true;
                }
            }
        }
        return isSupportVideoDecode && isSupportAudioDecode;
    }
}
