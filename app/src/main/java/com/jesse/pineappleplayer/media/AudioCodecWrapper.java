package com.jesse.pineappleplayer.media;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by zhaoliangtai on 2017/8/7.
 */

public class AudioCodecWrapper {
    private static final String TAG = AudioCodecWrapper.class.getSimpleName();
    private static final int TIMEOUT_US = 1000;
    private MediaCodec mDecoder;
    private boolean eosReceived;
    private int mSampleRate = 0;            // 采样率
    private int mChannel = 0;               // 通道

    private AudioCodecWrapper(MediaCodec codec, int sampleRate, int channel) {
        mDecoder = codec;
        codec.start();
        mSampleRate = sampleRate;
        mChannel = channel;
    }

    public static AudioCodecWrapper fromAudioFormat(MediaFormat trackFormat) throws IOException {
        AudioCodecWrapper result = null;
        MediaCodec audioCodec = null;
        int sampleRate = 0, channel = 0;
        long duration;
        String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
        if (mimeType.contains("audio/")) {
            sampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            channel = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            duration = trackFormat.getLong(MediaFormat.KEY_DURATION);
            Log.i(TAG, "duration: " + duration / 1000000);
            audioCodec = MediaCodec.createDecoderByType(mimeType);
            audioCodec.configure(trackFormat, null, null, 0);
        }
        if (audioCodec != null) {
            result = new AudioCodecWrapper(audioCodec, sampleRate, channel);
        }

        return result;
    }
}
