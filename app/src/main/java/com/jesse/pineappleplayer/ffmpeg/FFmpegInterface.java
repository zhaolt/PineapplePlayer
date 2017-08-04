package com.jesse.pineappleplayer.ffmpeg;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class FFmpegInterface {

    private static final String TAG = FFmpegInterface.class.getSimpleName();

    private static Map<String, OnFrameDecodeListener> mOnFrameDecodeGroup;


    private static FFmpegInterface instance;

    private FFmpegInterface() {
        mOnFrameDecodeGroup = new HashMap<>();
    }

    public static FFmpegInterface getInstance() {
        if (null == instance) {
            synchronized (FFmpegInterface.class) {
                if (null == instance) {
                    instance = new FFmpegInterface();
                }
            }
        }
        return instance;
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


    public native int initH264Decoder();
    public native int decodeH264Data(byte[] srcData, int dataLen, byte[] outData);
    public native int decodeFile(String url);

    public void sendData2Java(byte[] data) {
        Log.e(TAG, "sendData2Java");
        if (null == mOnFrameDecodeGroup || mOnFrameDecodeGroup.isEmpty()) {
            return;
        }
        for (Map.Entry<String, OnFrameDecodeListener> entry : mOnFrameDecodeGroup.entrySet()) {
            entry.getValue().onFrameDecode(data);
       }
    }

    public void updateSize(int w, int h) {
        Log.e(TAG, "width: " + w + ", height: " + h);
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
