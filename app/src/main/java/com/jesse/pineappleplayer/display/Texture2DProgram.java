package com.jesse.pineappleplayer.display;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * Created by zhaoliangtai on 17/3/22.
 */

public class Texture2DProgram {
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 vPosition;\n" +
                    "attribute vec4 a_texCoord;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * vPosition;\n" +
                    "    texCoord = (uTexMatrix * a_texCoord).xy;\n" +
                    "}";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 texCoord;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(s_texture, texCoord);\n" +
                    "}";
    /**
     * 灰阶
     */
    private static final String FRAGMENT_GREY_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);\n" +
                    "void main() {\n" +
                    "    vec4 color = texture2D(sTexture, v_texCoord);\n" +
                    "    float monoColor = dot(color.rgb, monoMultiplier);\n" +
                    "    gl_FragColor = vec4(monoColor, monoColor, monoColor, 1.0);\n" +
                    "}";

    private static final String FRAGMENT_FISH_EYES =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "const float PI = 3.1415926535;\n" +
                    "const float aperture = 180.0;\n" +
                    "const float apertureHalf = 0.5 * aperture * (PI / 180.0);\n" +
                    "const float maxFactor = sin(apertureHalf);\n" +
                    "void main() {\n" +
                    "    vec2 pos = 2.0 * v_texCoord.st - 1.0;\n" +
                    "    float l = length(pos);\n" +
                    "    if(l > 1.0) {\n" +
                    "        gl_FragColor = vec4(0, 0, 0, 1);\n" +
                    "    } else {\n" +
                    "        float x = maxFactor * pos.x;\n" +
                    "        float y = maxFactor * pos.y;\n" +
                    "        float n = length(vec2(x, y));\n" +
                    "        float z = sqrt(1.0 - n * n);\n" +
                    "        float r = atan(n, z) / PI;\n" +
                    "        float phi = atan(y, x);\n" +
                    "        float u = r * cos(phi) + 0.5;\n" +
                    "        float v = r * sin(phi) + 0.5;\n" +
                    "        gl_FragColor = texture2D(sTexture, vec2(u, v));\n" +
                    "    }\n" +
                    "}";

    private static final String FRAGMENT_RELIEF_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "const vec2 texSize = vec2(1440, 2250);\n" +
                    "void main() {\n" +
                    "    vec2 tex = v_texCoord;\n" +
                    "    vec2 upLeftUV = vec2(tex.x - 2.0 / texSize.x, tex.y  - 2.0 / texSize.y);\n" +
                    "    vec4 curColor = texture2D(sTexture,v_texCoord);\n" +
                    "    vec4 upLeftColor = texture2D(sTexture,upLeftUV);\n" +
                    "    vec4 delColor = curColor - upLeftColor;\n" +
                    "    float h = 0.3*delColor.x + 0.59*delColor.y + 0.11*delColor.z;\n" +
                    "    vec4 bkColor = vec4(0.5, 0.5, 0.5, 1.0);\n" +
                    "    gl_FragColor = vec4(h,h,h,0.0) +bkColor;\n" +
                    "}";


    private static final String FRAGMENT_STRING_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision highp float;\n" +
                    "uniform mediump samplerExternalOES tex1;\n" +
                    "uniform mediump float ts;\n" +
                    "uniform mediump float t;\n" +
                    "uniform mediump float p;\n" +
                    "\n" +
                    "varying vec2 texCoord;\n" +
                    "\n" +
                    "const highp vec3 W = vec3(0.299,0.587,0.114);\n" +
                    "const mat3 saturateMatrix = mat3(\n" +
                    "                                 1.1102,-0.0598,-0.061,\n" +
                    "                                 -0.0774,1.0826,-0.1186,\n" +
                    "                                 -0.0228,-0.0228,1.1772);\n" +
                    "\n" +
                    "float hardlight(float color)\n" +
                    "{\n" +
                    "    if(color <= 0.5)\n" +
                    "    {\n" +
                    "        color = color * color * 2.0;\n" +
                    "    }\n" +
                    "    else\n" +
                    "    {\n" +
                    "        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
                    "    }\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "void main(){\n" +
                    "    vec2 blurCoordinates[24];\n" +
                    "    \n" +
                    "    vec2 singleStepOffset = vec2(1.0 / 480.0, 1.0 / 480.0);\n" +
                    "    \n" +
                    "    vec4 params;\n" +
                    "\n" +
                    "    params = vec4(0.6, 0.8, 0.25, 0.25);\n" +
                    "    \n" +
                    "    blurCoordinates[0] = texCoord.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
                    "    blurCoordinates[1] = texCoord.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
                    "    blurCoordinates[2] = texCoord.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
                    "    blurCoordinates[3] = texCoord.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
                    "    \n" +
                    "    blurCoordinates[4] = texCoord.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
                    "    blurCoordinates[5] = texCoord.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
                    "    blurCoordinates[6] = texCoord.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
                    "    blurCoordinates[7] = texCoord.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
                    "    \n" +
                    "    blurCoordinates[8] = texCoord.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
                    "    blurCoordinates[9] = texCoord.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
                    "    blurCoordinates[10] = texCoord.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
                    "    blurCoordinates[11] = texCoord.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
                    "    \n" +
                    "    blurCoordinates[12] = texCoord.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
                    "    blurCoordinates[13] = texCoord.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
                    "    blurCoordinates[14] = texCoord.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
                    "    blurCoordinates[15] = texCoord.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
                    "    \n" +
                    "    blurCoordinates[16] = texCoord.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
                    "    blurCoordinates[17] = texCoord.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
                    "    blurCoordinates[18] = texCoord.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
                    "    blurCoordinates[19] = texCoord.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
                    "    \n" +
                    "    blurCoordinates[20] = texCoord.xy + singleStepOffset * vec2(-2.0, -2.0);\n" +
                    "    blurCoordinates[21] = texCoord.xy + singleStepOffset * vec2(-2.0, 2.0);\n" +
                    "    blurCoordinates[22] = texCoord.xy + singleStepOffset * vec2(2.0, -2.0);\n" +
                    "    blurCoordinates[23] = texCoord.xy + singleStepOffset * vec2(2.0, 2.0);\n" +
                    "    \n" +
                    "    \n" +
                    "    float sampleColor = texture2D(tex1, texCoord).g * 22.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[0]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[1]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[2]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[3]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[4]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[5]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[6]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[7]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[8]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[9]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[10]).g;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[11]).g;\n" +
                    "    \n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[12]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[13]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[14]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[15]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[16]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[17]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[18]).g * 2.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[19]).g * 2.0;\n" +
                    "    \n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[20]).g * 3.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[21]).g * 3.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[22]).g * 3.0;\n" +
                    "    sampleColor += texture2D(tex1, blurCoordinates[23]).g * 3.0;\n" +
                    "    \n" +
                    "    sampleColor = sampleColor / 62.0;\n" +
                    "    \n" +
                    "    vec3 centralColor = texture2D(tex1, texCoord).rgb;\n" +
                    "    \n" +
                    "    float highpass = centralColor.g - sampleColor + 0.5;\n" +
                    "    \n" +
                    "    for(int i = 0; i < 5;i++)\n" +
                    "    {\n" +
                    "        highpass = hardlight(highpass);\n" +
                    "    }\n" +
                    "    float lumance = dot(centralColor, W);\n" +
                    "    \n" +
                    "    float alpha = pow(lumance, params.r);\n" +
                    "    \n" +
                    "    vec3 smoothColor = centralColor + (centralColor-vec3(highpass))*alpha*0.1;\n" +
                    "\n" +
                    "    \n" +
                    "    smoothColor.r = clamp(pow(smoothColor.r, params.g),0.0,1.0);\n" +
                    "    smoothColor.g = clamp(pow(smoothColor.g, params.g),0.0,1.0);\n" +
                    "    smoothColor.b = clamp(pow(smoothColor.b, params.g),0.0,1.0);\n" +
                    "    \n" +
                    "    vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);\n" +
                    "    vec3 bianliang = max(smoothColor, centralColor);\n" +
                    "    vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;\n" +
                    "    \n" +
                    "    gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);\n" +
                    "    \n" +
                    "    gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);\n" +
                    "    gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);\n" +
                    "    \n" +
                    "    vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n" +
                    "    gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);\n" +
                    "}\n";

    private static final String FRAGMENT_NEGATIVE_COLOR_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "\n" +
                    "precision mediump float;\n" +
                    "\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "\n" +
                    "void main() {\n" +
                    "  vec4 color = texture2D(sTexture, v_texCoord);\n" +
                    "  gl_FragColor = vec4(1.0-color.r, 1.0-color.g, 1.0-color.b, 1.0);\n" +
                    "}";
    /**
     * #extension GL_OES_EGL_image_external : require
     * <p>
     * precision mediump float;
     * <p>
     * varying vec2 vTextureCoord;
     * uniform samplerExternalOES sTexture;
     * <p>
     * void main() {
     * vec2 uv = vTextureCoord;
     * if (vTextureCoord.x>0.5){
     * uv.x = 1.0 - vTextureCoord.x;
     * }
     * gl_FragColor = texture2D(sTexture, uv);
     * }
     */

    private static final String FRAGMENT_MIRROR_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    vec2 uv = v_texCoord;\n" +
                    "    uv.y = 1.0 - v_texCoord.y;\n" +
                    "    gl_FragColor = texture2D(sTexture, uv);\n" +
                    "}";

    private int mTextureTarget;
    private int mProgram;
    private int mPositionLocation;
    private int mTexCoordLocation;
    private int mvpMatrixLocation;
    private int texMatrixLocation;

    public Texture2DProgram() {
        mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mProgram = GlUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("Unable to create program");
        }
        mPositionLocation = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GlUtils.checkLocation(mPositionLocation, "vPosition");
        mTexCoordLocation = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        GlUtils.checkLocation(mTexCoordLocation, "a_texCoord");
        mvpMatrixLocation = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlUtils.checkLocation(mvpMatrixLocation, "uMVPMatrix");
        texMatrixLocation = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        GlUtils.checkLocation(texMatrixLocation, "uTexMatrix");
    }

    public int createTextureObj() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtils.checkGlError("glGenTextures");
        int textureID = textures[0];
        GLES20.glBindTexture(mTextureTarget, textureID);
        GlUtils.checkGlError("glBindTexture " + textureID);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtils.checkGlError("glTexParameter");
        return textureID;
    }

    public int[] createTextures() {
        int[] textures = new int[2];
        GLES20.glGenTextures(2, textures, 0);
        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(mTextureTarget, textures[i]);
            GlUtils.checkGlError("glBindTexture " + textures[i]);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            GlUtils.checkGlError("glTexParameter");
        }
        return textures;
    }

    public void release() {
        GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
    }

    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex, int vertexCount, int coordsPerVertex,
                     int vertexStride, float[] texMatrix, FloatBuffer textureBuffer, int textureId,
                     int textureStride) {
        GlUtils.checkGlError("start draw");
        GLES20.glUseProgram(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);
        GLES20.glUniformMatrix4fv(mvpMatrixLocation, 1, false, mvpMatrix, 0);
        GlUtils.checkGlError("glUniformMatrix4fv");
        GLES20.glUniformMatrix4fv(texMatrixLocation, 1, false, texMatrix, 0);
        GlUtils.checkGlError("glUniformMatrix4fv");
        GLES20.glEnableVertexAttribArray(mPositionLocation);
        GLES20.glVertexAttribPointer(mPositionLocation, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtils.checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(mTexCoordLocation);
        GlUtils.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(mTexCoordLocation, 2,
                GLES20.GL_FLOAT, false, textureStride, textureBuffer);
        GlUtils.checkGlError("glVertexAttribPointer");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtils.checkGlError("glDrawArrays");
        GLES20.glDisableVertexAttribArray(mPositionLocation);
        GLES20.glDisableVertexAttribArray(mTexCoordLocation);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }
}
