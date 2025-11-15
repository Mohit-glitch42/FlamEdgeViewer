package com.example.flamedgeviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Camera2Preview {
    private Activity activity;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession session;
    private Handler backgroundHandler;
    private ImageReader imageReader;

    public Camera2Preview(Activity activity, TextureView textureView) {
        this.activity = activity;
        this.textureView = textureView;
        startBackgroundThread();
    }

    public void start() {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            int width = 640, height = 480; // Can adjust to preferred preview size

            imageReader = ImageReader.newInstance(
                    width, height,
                    ImageFormat.YUV_420_888,
                    2
            );
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    // Extract Y plane as example
                    Image.Plane yPlane = image.getPlanes()[0];
                    ByteBuffer buffer = yPlane.getBuffer();
                    byte[] yBytes = new byte[buffer.remaining()];
                    buffer.get(yBytes);

                    // Call native
                    if (activity instanceof MainActivity) {
                        ((MainActivity)activity).processFrame(yBytes, width, height);
                    }

                    image.close();
                }
            }, backgroundHandler);

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(CameraDevice camera) { camera.close(); }
                @Override
                public void onError(CameraDevice camera, int error) { camera.close(); }
            }, backgroundHandler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startPreview() {
        Surface surface = new Surface(textureView.getSurfaceTexture());
        Surface imageSurface = imageReader.getSurface();

        try {
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, imageSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Camera2Preview.this.session = session;
                            try {
                                CaptureRequest.Builder builder =
                                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(surface);
                                builder.addTarget(imageSurface);
                                session.setRepeatingRequest(builder.build(), null, backgroundHandler);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {}
                    }, backgroundHandler
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startBackgroundThread() {
        HandlerThread thread = new HandlerThread("Camera2Thread");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());
    }

    public void stop() {
        if (session != null) session.close();
        if (cameraDevice != null) cameraDevice.close();
        if (imageReader != null) imageReader.close();
    }
}
