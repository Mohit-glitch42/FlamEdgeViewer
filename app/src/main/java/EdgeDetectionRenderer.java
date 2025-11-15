package com.example.flamedgeviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EdgeDetectionRenderer implements GLSurfaceView.Renderer {
    private final float[] vtx = { -1, 1, -1, -1, 1, 1, 1, -1 };
    private final float[] txc = { 0, 0, 0, 1, 1, 0, 1, 1 };
    private FloatBuffer vbo, tbo;
    private int program, posH, texH, texU, textureId;
    private int frameWidth = 640, frameHeight = 480;
    private ByteBuffer frameBuffer;

    public EdgeDetectionRenderer() {
        vbo = ByteBuffer.allocateDirect(vtx.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vbo.put(vtx).position(0);
        tbo = ByteBuffer.allocateDirect(txc.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        tbo.put(txc).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vsh = "attribute vec4 aP;attribute vec2 aT;varying vec2 vT;void main(){gl_Position=aP;vT=aT;}";
        String fsh = "precision mediump float;varying vec2 vT;uniform sampler2D uT;void main(){gl_FragColor=texture2D(uT,vT);}";
        int vShader = compileShader(GLES20.GL_VERTEX_SHADER, vsh);
        int fShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fsh);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);
        posH = GLES20.glGetAttribLocation(program, "aP");
        texH = GLES20.glGetAttribLocation(program, "aT");
        texU = GLES20.glGetUniformLocation(program, "uT");
        textureId = createTexture();
        frameBuffer = ByteBuffer.allocateDirect(frameWidth * frameHeight).order(ByteOrder.nativeOrder());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        GLES20.glViewport(0, 0, w, h);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);
        GLES20.glEnableVertexAttribArray(posH);
        GLES20.glVertexAttribPointer(posH, 2, GLES20.GL_FLOAT, false, 0, vbo);
        GLES20.glEnableVertexAttribArray(texH);
        GLES20.glVertexAttribPointer(texH, 2, GLES20.GL_FLOAT, false, 0, tbo);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        frameBuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, frameWidth, frameHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameBuffer);
        GLES20.glUniform1i(texU, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(posH);
        GLES20.glDisableVertexAttribArray(texH);
    }

    public void updateFrame(byte[] pixels, int width, int height) {
        if (width != frameWidth || height != frameHeight) {
            frameWidth = width;
            frameHeight = height;
            frameBuffer = ByteBuffer.allocateDirect(frameWidth * frameHeight).order(ByteOrder.nativeOrder());
        }
        frameBuffer.position(0);
        frameBuffer.put(pixels);
    }

    private int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return textures[0];
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
