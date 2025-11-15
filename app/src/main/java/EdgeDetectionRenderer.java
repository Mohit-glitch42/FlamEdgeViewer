package com.example.flamedgeviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class EdgeDetectionRenderer implements GLSurfaceView.Renderer {
    private int textureId = -1;
    private int program;
    private ByteBuffer pixelBuffer;
    private int width, height;

    private final float[] vertexData = {
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    private FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData);

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexShaderCode = "attribute vec2 aPosition;" +
                "varying vec2 vTexCoord;" +
                "void main() {" +
                "  vTexCoord = aPosition * 0.5 + 0.5;" +
                "  gl_Position = vec4(aPosition, 0.0, 1.0);" +
                "}";

        String fragmentShaderCode = "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vTexCoord;" +
                "void main() {" +
                "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
                "}";

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        // Create texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (pixelBuffer == null) return;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);

        int aPositionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        int uTextureLocation = GLES20.glGetUniformLocation(program, "uTexture");

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        pixelBuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);

        GLES20.glUniform1i(uTextureLocation, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(aPositionLocation);
    }

    // Call this to update RGBA frame bytes from native
    public void updateFrameRGBA(byte[] rgbaPixels, int width, int height) {
        this.width = width;
        this.height = height;

        if (pixelBuffer == null || pixelBuffer.capacity() != rgbaPixels.length) {
            pixelBuffer = ByteBuffer.allocateDirect(rgbaPixels.length).order(ByteOrder.nativeOrder());
        }
        pixelBuffer.clear();
        pixelBuffer.put(rgbaPixels);
        pixelBuffer.position(0);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
