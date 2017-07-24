package com.jesse.pineappleplayer.media;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class MediaCodecWrapper {

    private MediaCodec mDecoder;
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;

    private Queue<Integer> mAvailableInputBuffers;
    private Queue<Integer> mAvailableOutputBuffers;

    private MediaCodec.BufferInfo[] mOutputBufferInfo;

    static MediaCodec.CryptoInfo cryptoInfo = new MediaCodec.CryptoInfo();

    private MediaCodecWrapper(MediaCodec codec) {
        mDecoder = codec;
        codec.start();
        mInputBuffers = codec.getInputBuffers();
        mOutputBuffers = codec.getOutputBuffers();
        mAvailableInputBuffers = new ArrayDeque<>(mOutputBuffers.length);
        mAvailableOutputBuffers = new ArrayDeque<>(mInputBuffers.length);
    }

    public static MediaCodecWrapper fromVideoFormat(MediaFormat trackFormat, Surface surface) throws IOException {
        MediaCodecWrapper result = null;
        MediaCodec videoCodec = null;
        String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
        if (mimeType.contains("video/")) {
            videoCodec = MediaCodec.createDecoderByType(mimeType);
            videoCodec.configure(trackFormat, surface, null, 0);
        }
        if (videoCodec != null) {
            result = new MediaCodecWrapper(videoCodec);
        }
        return result;
    }

    public boolean writeSample(MediaExtractor extractor, boolean isSecure, long presentationTimeUs, int flags) {
        boolean result = false;
        boolean isEos = false;

        if (!mAvailableInputBuffers.isEmpty()) {
            int index = mAvailableInputBuffers.remove();
            ByteBuffer buffer = mInputBuffers[index];

            int size = extractor.readSampleData(buffer, 0);
            if (size <= 0) {
                flags |= MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }

            if (!isSecure) {
                mDecoder.queueInputBuffer(index, 0, size, presentationTimeUs, flags);
            } else {
                extractor.getSampleCryptoInfo(cryptoInfo);
                mDecoder.queueSecureInputBuffer(index, 0, cryptoInfo, presentationTimeUs, flags);
            }
            result = true;
        }
        return result;
    }

    public boolean peekSample(MediaCodec.BufferInfo outBufferInfo) {
        update();
        boolean result = false;
        if (!mAvailableOutputBuffers.isEmpty()) {
            int index = mAvailableOutputBuffers.peek();
            MediaCodec.BufferInfo info = mOutputBufferInfo[index];
            outBufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags);
            result = true;
        }
        return result;
    }

    private void update() {
        int index;
        while ((index = mDecoder.dequeueInputBuffer(0)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            mAvailableInputBuffers.add(index);
        }
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while ((index = mDecoder.dequeueOutputBuffer(info, 0)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            switch (index) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    mOutputBuffers = mDecoder.getOutputBuffers();
                    mOutputBufferInfo = new MediaCodec.BufferInfo[mOutputBuffers.length];
                    mAvailableOutputBuffers.clear();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    break;
                default:
                    if (index >= 0) {
                        mOutputBufferInfo[index] = info;
                        mAvailableOutputBuffers.add(index);
                    } else {
                        throw new IllegalStateException("Unknow status from dequeueOutputBuffer");
                    }
                    break;
            }
        }
    }

    public void stopAndRelease() {
        mDecoder.stop();
        mDecoder.release();
        mDecoder = null;
    }

    public void popSample(boolean render) {
        update();
        if (!mAvailableOutputBuffers.isEmpty()) {
            int index = mAvailableOutputBuffers.remove();
            mDecoder.releaseOutputBuffer(index, render);
        }
    }
}
