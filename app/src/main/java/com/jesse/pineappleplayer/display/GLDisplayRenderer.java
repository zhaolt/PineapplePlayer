package com.jesse.pineappleplayer.display;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.view.Surface;

import com.jesse.pineappleplayer.R;
import com.jesse.pineappleplayer.media.MediaParser;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhaoliangtai on 2017/7/25.
 */

public class GLDisplayRenderer implements GLSurfaceView.Renderer {

    private FullFrameRect mFullFrameRect;
    private int textureID;
    private SurfaceTexture mSurfaceTexture;
    private float[] stMatrix = new float[16];

    private GLSurfaceView mGLSurfaceView;

    public GLDisplayRenderer(GLSurfaceView surfaceView) {
        mGLSurfaceView = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFullFrameRect = new FullFrameRect(new Texture2DProgram());
        textureID = mFullFrameRect.createTextureObj();
        mSurfaceTexture = new SurfaceTexture(textureID);
        mSurfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
        new Handler(mGLSurfaceView.getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("android.resource://" + mGLSurfaceView.getContext().getPackageName() + '/' + R.raw.vid_bigbuckbunny);
                MediaParser.getInstance().startPlayback(mGLSurfaceView.getContext(), uri, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "b.mp4", new Surface(mSurfaceTexture));
            }
        });
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (null == mSurfaceTexture || null == mFullFrameRect)
            return;
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(stMatrix);
        mFullFrameRect.drawFrame(textureID, stMatrix);
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mGLSurfaceView.requestRender();
        }
    };
}
