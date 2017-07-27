package com.jesse.pineappleplayer.display;

/**
 * Created by zhaoliangtai on 17/3/22.
 */

public class FullFrameRect {

    private final Drawable2d mDrawable2d = new Drawable2d();
    private Texture2DProgram mTexture2DProgram;

    public FullFrameRect(Texture2DProgram program) {
        mTexture2DProgram = program;
    }

    public void release(boolean doEglCleanup) {
        if (null != mTexture2DProgram) {
            if (doEglCleanup) {
                mTexture2DProgram.release();
            }
        }
        mTexture2DProgram = null;
    }

    public Texture2DProgram getProgram() {
        return mTexture2DProgram;
    }

    public void changeProgram(Texture2DProgram program) {
        mTexture2DProgram.release();
        mTexture2DProgram = program;
    }

    public int createTextureObj() {
        return mTexture2DProgram.createTextureObj();
    }

    public int[] createTextures() {
        return mTexture2DProgram.createTextures();
    }

    public void drawFrame(int textureId, float[] texMatrix) {
        mTexture2DProgram.draw(GlUtils.IDENTITY_MATRIX, mDrawable2d.getVertexBuffer(), 0, mDrawable2d.getVertexCount(),
                mDrawable2d.getCoordPerVertex(), mDrawable2d.getVertexStride(), texMatrix,
                mDrawable2d.getTextureCoordBuffer(), textureId, mDrawable2d.getTexCoordStride());
    }
}
