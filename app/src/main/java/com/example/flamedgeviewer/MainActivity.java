package com.example.flamedgeviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

// Make sure Camera2Preview.java is present in the same package
public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private TextureView textureView;
    private Camera2Preview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link to the TextureView in activity_main.xml
        textureView = findViewById(R.id.textureView);

        // Request camera permission if not yet granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }
    }

    // Called after permission request result
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
        cameraPreview = new Camera2Preview(this, textureView);
        cameraPreview.start();
    }

    // Ensure camera stops when app is not active
    @Override
    protected void onStop() {
        super.onStop();
        if (cameraPreview != null) cameraPreview.stop();
    }
}
