package com.example.flamedgeviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private int filterIndex = 0; // 0:Edge, 1:Gray, 2:Blur, 3:None

    private static final int CAMERA_PERMISSION_CODE = 100;
    private GLSurfaceView glSurfaceView;
    private EdgeDetectionRenderer renderer;
    private Camera2Preview cameraPreview;

    private boolean captureNextFrame = false;

    private final int[] filterButtonIds = {
            R.id.btnEdge, R.id.btnGray, R.id.btnBlur, R.id.btnNone
    };

    public native void processFrame(byte[] yPlane, int width, int height, int filterType);

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MaterialButtonToggleGroup filterGroup = findViewById(R.id.filterGroup);
        filterGroup.check(R.id.btnEdge);
        filterGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                for (int i = 0; i < filterButtonIds.length; ++i) {
                    if (checkedId == filterButtonIds[i]) {
                        filterIndex = i;
                        break;
                    }
                }
            }
        });

        FloatingActionButton btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(v -> captureNextFrame = true);

        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new EdgeDetectionRenderer();
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        tryStartCamera();
    }

    public int getFilterIndex() {
        return filterIndex;
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

    // Called from native code for grayscale/edge
    public void updateProcessedFrame(byte[] pixels, int width, int height) {
        runOnUiThread(() -> {
            renderer.updateFrameRGBA(pixels, width, height);
            glSurfaceView.requestRender();

            if (captureNextFrame) {
                saveEdgeImage(pixels, width, height);
                captureNextFrame = false;
            }
        });
    }

    // Called from native code for RGBA frames
    public void updateProcessedFrameRGBA(byte[] pixels, int width, int height) {
        runOnUiThread(() -> {
            renderer.updateFrameRGBA(pixels, width, height);
            glSurfaceView.requestRender();

            if (captureNextFrame) {
                saveEdgeImageRGBA(pixels, width, height);
                captureNextFrame = false;
            }
        });
    }

    private void saveEdgeImage(byte[] pixels, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] colors = new int[width * height];
        for (int i = 0; i < colors.length; i++) {
            int gray = pixels[i] & 0xFF;
            colors[i] = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        }
        bitmap.setPixels(colors, 0, width, 0, 0, width, height);

        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "flamedgeviewer");
        if (!path.exists()) path.mkdirs();
        String filename = "edge_capture_" + System.currentTimeMillis() + ".png";
        File outFile = new File(path, filename);

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "Saved: " + outFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveEdgeImageRGBA(byte[] pixels, int width, int height) {
        int[] colors = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int r = pixels[i * 4] & 0xFF;
            int g = pixels[i * 4 + 1] & 0xFF;
            int b = pixels[i * 4 + 2] & 0xFF;
            int a = pixels[i * 4 + 3] & 0xFF;
            colors[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        Bitmap bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);

        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "flamedgeviewer");
        if (!path.exists()) path.mkdirs();
        String filename = "edge_capture_" + System.currentTimeMillis() + ".png";
        File outFile = new File(path, filename);

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "Saved: " + outFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
