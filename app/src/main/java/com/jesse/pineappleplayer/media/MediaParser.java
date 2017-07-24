package com.jesse.pineappleplayer.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class MediaParser {

    // 媒体数据提取器 由它来从多媒体文件中提取压缩编码数据 比如从一个.mp4文件中提取H264数据
    private MediaExtractor mMediaExtractor;

    // MediaCodec包装类 核心解码器
    private MediaCodecWrapper mCodecWrapper;

    private MediaParser() {
        mMediaExtractor = new MediaExtractor();
    }

    public static class SingleTon {
        public static final MediaParser INSTANCE = new MediaParser();
    }

    public static MediaParser getInstance() {
        return SingleTon.INSTANCE;
    }

    public void startPlayback(Context context, Uri uri, String url, Surface surface) {
        try {
//            mMediaExtractor.setDataSource(url);
            mMediaExtractor.setDataSource(context, uri, null);
            // 获取轨道数量
            int nTracks = mMediaExtractor.getTrackCount();
            /**
             * 取消选择提取器里所有轨道
             */
            for (int i = 0; i < nTracks; ++i) {
                mMediaExtractor.unselectTrack(i);
            }
            // 在流里找到第一个视频轨道。
            for (int i = 0; i < nTracks; ++i) {
                mCodecWrapper = MediaCodecWrapper.fromVideoFormat(mMediaExtractor.getTrackFormat(i),
                        surface);
                if (mCodecWrapper != null) {
                    mMediaExtractor.selectTrack(i);
                    break;
                }
            }

            boolean isEos = (mMediaExtractor.getSampleFlags() & MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    == MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            if (!isEos) {
                boolean result = mCodecWrapper.writeSample(mMediaExtractor, false,
                        mMediaExtractor.getSampleTime(), mMediaExtractor.getSampleFlags());
                if (result) {
                    // 下一个样本
                    mMediaExtractor.advance();
                }
            }

            MediaCodec.BufferInfo outBufferInfo = new MediaCodec.BufferInfo();
            mCodecWrapper.peekSample(outBufferInfo);
            if (outBufferInfo.size <= 0 && isEos) {
                mCodecWrapper.stopAndRelease();
                mMediaExtractor.release();
            } else {
                mCodecWrapper.popSample(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
