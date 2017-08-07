package com.jesse.pineappleplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jesse.pineappleplayer.base.DisplayActivity;
import com.jesse.pineappleplayer.display.GLDisplayView;

/**
 * Created by zhaoliangtai on 2017/7/25.
 */

public class TempPlayActivity extends DisplayActivity {

    private GLDisplayView mPlaybackView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        mPlaybackView = (GLDisplayView) findViewById(R.id.playback_view);


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "b.mp4";
//                MediaParser.getInstance().decodeFileByFFmpeg(path, mPlaybackView, TempPlayActivity.class.getSimpleName());
//            }
//        }).start();

    }
}
