package com.jesse.pineappleplayer.display;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by zhaoliangtai on 17/3/22.
 */

public class GlUtils {

    private static final String TAG = GlUtils.class.getName();
    public static final int SIZEOF_FLOAT = 4;

    public static final float[] IDENTITY_MATRIX;
    static {
        IDENTITY_MATRIX = new float[16];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);            // 矩阵归位
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) return 0;
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) return 0;
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0)
            Log.e(TAG, "Could not Create program.");
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader vertex");
        GLES20.glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader fragment");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private static int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        checkGlError("glCreateShader type = " + type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + type + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    public static FloatBuffer createFloatBuffer(float[] fullRectangleCoords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(fullRectangleCoords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(fullRectangleCoords);
        fb.position(0);
        return fb;
    }

    public static ShortBuffer createShortBuffer(short[] fullRectangleCoords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(fullRectangleCoords.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(fullRectangleCoords);
        sb.position(0);
        return sb;
    }
}
