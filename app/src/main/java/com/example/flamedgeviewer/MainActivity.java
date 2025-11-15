package com.example.flamedgeviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private GLSurfaceView glSurfaceView;
    private EdgeDetectionRenderer renderer;
    private Camera2Preview cameraPreview;

    public native void processFrame(byte[] yPlane, int width, int height);
    public native String stringFromJNI();

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new EdgeDetectionRenderer();
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        String result = stringFromJNI();
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        tryStartCamera();
    }

    private void tryStartCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        if (cameraPreview == null) {
            cameraPreview = new Camera2Preview(this);
            cameraPreview.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraPreview != null) cameraPreview.stop();
    }

    // Called from native code to update frame pixels for OpenGL rendering
    public void updateProcessedFrame(byte[] pixels, int width, int height) {
        runOnUiThread(() -> {
            renderer.updateFrame(pixels, width, height);
            glSurfaceView.requestRender();
        });
    }
}
