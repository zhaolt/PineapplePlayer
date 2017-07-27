package com.jesse.pineappleplayer.display;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.jesse.pineappleplayer.display.GlUtils.SIZEOF_FLOAT;


/**
 * Created by zhaoliangtai on 17/3/22.
 */

public class Drawable2d {
    private static final float FLOAT_SIZE = 4;
    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };


    private static float shapeCoordsFishEye[] = {
            -1.0f,  -0.6428f,    // top left
            1.0f, -0.6428f,    // bottom left
            -1.0f, 0.6428f,   // bottom right
            1.0f,  0.6428f,
    }; // top right

    private static float textureCoords[] = {
            0.0f,  1.0f,   // top left
            1.0f, 1.0f,   // bottom left
            1.0f, 0.0f,    // bottom right
            0.0f,  0.0f}; // top right

    private static final short DRAW_ORDER[] = { 0, 1, 2, 0, 2, 3};
    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtils.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtils.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
    private static final ShortBuffer DRAW_ORDER_BUF =
            GlUtils.createShortBuffer(DRAW_ORDER);
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordBuffer;



    private ShortBuffer drawOrderBuffer;
    private int vertexCount;
    private int coordPerVertex;
    private int vertexStride;

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public FloatBuffer getTextureCoordBuffer() {
        return textureCoordBuffer;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getCoordPerVertex() {
        return coordPerVertex;
    }

    public int getVertexStride() {
        return vertexStride;
    }

    public int getTexCoordStride() {
        return texCoordStride;
    }

    private int texCoordStride;

    public Drawable2d() {
        vertexBuffer = FULL_RECTANGLE_BUF;
        textureCoordBuffer = FULL_RECTANGLE_TEX_BUF;
        coordPerVertex = 2;
        vertexStride = coordPerVertex * SIZEOF_FLOAT;
        vertexCount = FULL_RECTANGLE_COORDS.length / coordPerVertex;
        texCoordStride = 2 * SIZEOF_FLOAT;
    }

    public ShortBuffer getDrawOrderBuffer() {
        return drawOrderBuffer;
    }

}
