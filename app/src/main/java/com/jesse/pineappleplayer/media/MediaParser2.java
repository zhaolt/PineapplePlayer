package com.jesse.pineappleplayer.media;

import android.media.MediaMetadataRetriever;

/**
 * Created by zhaoliangtai on 2017/8/7.
 */

public class MediaParser2 {

    private static final String TAG = MediaParser2.class.getSimpleName();

    private String url;

    private boolean isHasVideoTrack = false;

    private boolean isHasAudioTrack = false;

    private int videoFps;

    private int videoBitRate;

    private int videoWidth;

    private int videoHeight;

    private MediaMetadataRetriever mMediaMetadataRetriever;

    public MediaParser2(String url) {
        this.url = url;
        mMediaMetadataRetriever = new MediaMetadataRetriever();
        parse();
    }

    private void parse() {
        mMediaMetadataRetriever.setDataSource(url);
        if (mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO).equals("yes")) {
            isHasAudioTrack = true;
        } else {
            isHasAudioTrack = false;
        }
        if (mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO).equals("yes")) {
            isHasVideoTrack = true;
        } else {
            isHasVideoTrack = false;
        }
        if (isHasVideoTrack) {
            videoWidth = Integer.valueOf(mMediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            videoHeight = Integer.valueOf(mMediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            videoBitRate = Integer.valueOf(mMediaMetadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
        }
    }

}
