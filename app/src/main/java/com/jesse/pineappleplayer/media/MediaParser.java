package com.jesse.pineappleplayer.media;

import android.animation.TimeAnimator;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.jesse.pineappleplayer.display.GLDisplayView;
import com.jesse.pineappleplayer.ffmpeg.FFmpegInterface;

import java.io.IOException;

/**
 * Created by zhaoliangtai on 2017/7/24.
 */

public class MediaParser {

    private static final String TAG = MediaParser.class.getSimpleName();

    // 媒体数据提取器 由它来从多媒体文件中提取压缩编码数据 比如从一个.mp4文件中提取H264数据
    private MediaExtractor mMediaExtractor;

    // MediaCodec包装类 核心解码器
    private MediaCodecWrapper mCodecWrapper;

    private TimeAnimator mTimeAnimator = new TimeAnimator();

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
            mMediaExtractor.setDataSource(context, uri, null);
            int nTracks = mMediaExtractor.getTrackCount();

            for (int i = 0; i < nTracks; ++i) {
                mMediaExtractor.unselectTrack(i);
            }


            for (int i = 0; i < nTracks; ++i) {
                mCodecWrapper = MediaCodecWrapper.fromVideoFormat(mMediaExtractor.getTrackFormat(i),
                        surface);
                if (mCodecWrapper != null) {
                    mMediaExtractor.selectTrack(i);
                    break;
                }
            }
            mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
                @Override
                public void onTimeUpdate(final TimeAnimator animation,
                                         final long totalTime,
                                         final long deltaTime) {

                    boolean isEos = ((mMediaExtractor.getSampleFlags() & MediaCodec
                            .BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);


                    if (!isEos) {
                        boolean result = mCodecWrapper.writeSample(mMediaExtractor, false,
                                mMediaExtractor.getSampleTime(), mMediaExtractor.getSampleFlags());

                        if (result) {
                            mMediaExtractor.advance();
                        }
                    }


                    MediaCodec.BufferInfo out_bufferInfo = new MediaCodec.BufferInfo();
                    mCodecWrapper.peekSample(out_bufferInfo);


                    if (out_bufferInfo.size <= 0 && isEos) {
                        mTimeAnimator.end();
                        mCodecWrapper.stopAndRelease();
                        mMediaExtractor.release();
                    } else if (out_bufferInfo.presentationTimeUs / 1000 < totalTime) {
                        mCodecWrapper.popSample(true);
                    }


                }
            });

            mTimeAnimator.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decodeFileByFFmpeg(String url, final GLDisplayView displayView, String className) {
        FFmpegInterface.getInstance().addOnFrameDecodeListener(className, new FFmpegInterface.OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(byte[] data) {
                displayView.pushYUVData(data);
            }

            @Override
            public void updateSize(int w, int h) {
                displayView.updateSize(w, h);
            }
        });
        int result = FFmpegInterface.getInstance().decodeFile(url);
        switch (result) {
            case -1:
                Log.e(TAG, "decode error");
                FFmpegInterface.getInstance().deleteFrameDecodeListener(className);
                break;
            case 2:
                Log.i(TAG, "decode end");
                FFmpegInterface.getInstance().deleteFrameDecodeListener(className);
                break;
            default:
                Log.i(TAG, "decoding...");
                break;
        }
    }


    public interface OnFFmpegDecodeListener {
        void onDecodeData(byte[] outData);

        void updateSize(int w, int h);
    }
}
