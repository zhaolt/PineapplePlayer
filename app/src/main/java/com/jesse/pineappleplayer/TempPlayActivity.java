package com.jesse.pineappleplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jesse.pineappleplayer.base.DisplayActivity;
import com.jesse.pineappleplayer.display.GLDisplayView;

import java.io.File;

/**
 * Created by zhaoliangtai on 2017/7/25.
 */

public class TempPlayActivity extends DisplayActivity implements SurfaceHolder.Callback {

    private GLDisplayView mPlaybackView;

    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
        mPlaybackView = (GLDisplayView) findViewById(R.id.playback_view);

//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "b.mp4";
//        MediaParser.getInstance().decodeFileByFFmpeg(path, mPlaybackView, TempPlayActivity.class.getSimpleName());
    }


    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        new Thread(new Runnable() {
            String[] paths = {Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "asd.jpg",
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zxc.jpg"};
            int i = 0;
            @Override
            public void run() {
                while (true) {

                    Canvas c = holder.lockCanvas();
                    Bitmap b = BitmapFactory.decodeFile(paths[i++ % 2]);
                    c.drawBitmap(b, 0, 0, null);
                    b.recycle();
                    holder.unlockCanvasAndPost(c);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
