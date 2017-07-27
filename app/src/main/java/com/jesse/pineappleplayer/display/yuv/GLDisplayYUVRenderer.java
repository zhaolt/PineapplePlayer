package com.jesse.pineappleplayer.display.yuv;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhaoliangtai on 2017/7/25.
 */

public class GLDisplayYUVRenderer implements GLSurfaceView.Renderer {

    private GLSurfaceView mTargetSurface;

    private GLProgram mProgram = new GLProgram(0);

    private int mVideoWidth, mVideoHeight;

    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    private boolean sizeFlag = false;

    public void init(GLSurfaceView surfaceView) {
        mTargetSurface = surfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!mProgram.isProgramBuilt()) {
            mProgram.buildProgram();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (y != null) {
                // reset position, have to be done
                y.position(0);
                u.position(0);
                v.position(0);
                mProgram.buildTextures(y, u, v, mVideoWidth, mVideoHeight);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                mProgram.drawFrame();
            }
        }
    }

    public void update(int w, int h) {
        if (w > 0 && h > 0) {
            mProgram.createBuffers(GLProgram.squareVertices);
            // 初始化容器
            if (w != mVideoWidth && h != mVideoHeight) {
                this.mVideoWidth = w;
                if (h < 720) {
                    h-=8;
                }
                this.mVideoHeight = h;
                int yarraySize = w * h;
                int uvarraySize = yarraySize / 4;
                synchronized (this) {
                    y = ByteBuffer.allocate(yarraySize);
                    u = ByteBuffer.allocate(uvarraySize);
                    v = ByteBuffer.allocate(uvarraySize);
                }
            }
        }
    }

    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        synchronized (this) {
            clearCache();
            y.put(ydata, 0, ydata.length);
            u.put(udata, 0, udata.length);
            v.put(vdata, 0, vdata.length);
        }

        // request to render
        mTargetSurface.requestRender();
    }

    private void clearCache() {
        y.clear();
        u.clear();
        v.clear();
    }

    public void putYUV2Player(byte[] buffer) {
        if (sizeFlag) {
            int yLen = mVideoWidth * mVideoHeight;
            int uLen = yLen / 4;
            System.arraycopy(buffer, 0, y.array(), 0, yLen);
            System.arraycopy(buffer, yLen, u.array(), 0, uLen);
            System.arraycopy(buffer, yLen + uLen, v.array(), 0, uLen);
            update(y.array(), u.array(), v.array());
        }
    }

    public void changeSize(int w, int h) {
        update(w, h);
        sizeFlag = true;
    }

    public void release(boolean flag) {
        sizeFlag = flag;
    }
}
