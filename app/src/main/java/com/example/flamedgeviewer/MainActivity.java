package com.example.flamedgeviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private TextureView textureView;
    private Camera2Preview cameraPreview;

    // Native methods: declare only!
    public native void processFrame(byte[] yPlane, int width, int height);
    public native String stringFromJNI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);

        // Show JNI test toast
        String result = stringFromJNI();
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        // Attach TextureView listener
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                tryStartCamera();
            }
            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
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
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to start the camera preview using Camera2Preview class
    private void startCamera() {
        if (textureView.isAvailable()) {
            cameraPreview = new Camera2Preview(this, textureView);
            cameraPreview.start();
        }
    }

    // Ensure camera stops when app is not active
    @Override
    protected void onStop() {
        super.onStop();
        if (cameraPreview != null) cameraPreview.stop();
    }

    static {
        System.loadLibrary("native-lib");
    }
}
