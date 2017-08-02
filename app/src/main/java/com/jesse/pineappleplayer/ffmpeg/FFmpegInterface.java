package com.jesse.pineappleplayer.ffmpeg;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class FFmpegInterface {

    private Map<String, OnFrameDecodeListener> mOnFrameDecodeGroup;


    private FFmpegInterface() {
        mOnFrameDecodeGroup = new HashMap<>();
    }

    public static class SingleTon {
        public static final FFmpegInterface INSTANCE = new FFmpegInterface();
    }

    public void addOnFrameDecodeListener(String className, OnFrameDecodeListener listener) {
        mOnFrameDecodeGroup.put(className, listener);
    }

    public boolean deleteFrameDecodeListener(String className) {
        boolean result = false;
        if (mOnFrameDecodeGroup.containsKey(className)) {
            mOnFrameDecodeGroup.remove(className);
            result = true;
        }
        return result;
    }

    public static FFmpegInterface getInstance() {
        return SingleTon.INSTANCE;
    }

    public native int initH264Decoder();
    public native int decodeH264Data(byte[] srcData, int dataLen, byte[] outData);
    public native int decodeFile(String url);

    public void sendData2Java(byte[] data) {
        if (null == mOnFrameDecodeGroup || mOnFrameDecodeGroup.isEmpty()) {
            return;
        }
        for (Map.Entry<String, OnFrameDecodeListener> entry : mOnFrameDecodeGroup.entrySet()) {
            entry.getValue().onFrameDecode(data);
        }
    }

    public void updateSize(int w, int h) {
        if (null == mOnFrameDecodeGroup || mOnFrameDecodeGroup.isEmpty()) {
            return;
        }
        for (Map.Entry<String, OnFrameDecodeListener> entry : mOnFrameDecodeGroup.entrySet()) {
            entry.getValue().updateSize(w, h);
        }
    }

    public interface OnFrameDecodeListener {
        void onFrameDecode(byte[] data);
        void updateSize(int w, int h);
    }
}
