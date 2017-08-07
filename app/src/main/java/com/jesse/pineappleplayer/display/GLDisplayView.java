package com.jesse.pineappleplayer.display;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import com.jesse.pineappleplayer.App;
import com.jesse.pineappleplayer.display.yuv.GLDisplayYUVRenderer;

/**
 * Created by zhaoliangtai on 2017/7/25.
 */

public class GLDisplayView extends GLSurfaceView {

    private GLDisplayRenderer mTextureRenderer;

    private GLDisplayYUVRenderer mYUVRenderer;

    public GLDisplayView(Context context) {
        this(context, null);
    }

    public GLDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        if (App.isSupportMediaCodecDecode) {
            mTextureRenderer = new GLDisplayRenderer(this);
            setRenderer(mTextureRenderer);
        } else {
            mYUVRenderer = new GLDisplayYUVRenderer();
            mYUVRenderer.init(this);
            setRenderer(mYUVRenderer);
        }
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public Surface getSurface() {
        if (null != mTextureRenderer) {
            return new Surface(mTextureRenderer.getSurfaceTexture());
        }
        return null;
    }

    public void pushYUVData(byte[] data) {
        if (mYUVRenderer != null) {
            mYUVRenderer.putYUV2Player(data);
        }
    }

    public void updateSize(int w, int h) {
        if (mYUVRenderer != null) {
            mYUVRenderer.changeSize(w, h);
        }
    }
}
