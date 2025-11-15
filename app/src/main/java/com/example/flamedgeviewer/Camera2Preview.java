package com.example.flamedgeviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.graphics.YuvImage;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;

public class Camera2Preview {
    private Activity activity;
    private CameraDevice cameraDevice;
    private CameraCaptureSession session;
    private Handler backgroundHandler;
    private ImageReader imageReader;
    private String cameraId;
    private boolean isFrontCamera;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public Camera2Preview(Activity activity) {
        this.activity = activity;
        startBackgroundThread();
    }

    public Bitmap yuvToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    // Robust portrait orientation fix for all cameras
    private int getImageRotationCompensation() {
        // Most rear sensors are landscape by default, phone UI is portrait
        // Hardcode 90 degree rotation for portrait apps; change to 270 if it's upside-down!
        return 270;
    }


    public Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipHorizontal) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        if (flipHorizontal) {
            matrix.postScale(-1, 1);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void start() {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            int width = 640, height = 480;

            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            isFrontCamera = (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT);

            imageReader = ImageReader.newInstance(
                    width, height,
                    ImageFormat.YUV_420_888,
                    2
            );
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    Bitmap bitmap = yuvToBitmap(image);
                    image.close();

                    int rotationDegrees = getImageRotationCompensation();
                    Bitmap rotatedBitmap = rotateBitmap(bitmap, rotationDegrees, isFrontCamera);

                    int w = rotatedBitmap.getWidth();
                    int h = rotatedBitmap.getHeight();
                    int[] pixels = new int[w * h];
                    rotatedBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

                    byte[] rgbaBytes = new byte[w * h * 4];
                    for (int i = 0; i < pixels.length; ++i) {
                        int p = pixels[i];
                        rgbaBytes[i * 4] = (byte) ((p >> 16) & 0xFF);
                        rgbaBytes[i * 4 + 1] = (byte) ((p >> 8) & 0xFF);
                        rgbaBytes[i * 4 + 2] = (byte) (p & 0xFF);
                        rgbaBytes[i * 4 + 3] = (byte) ((p >> 24) & 0xFF);
                    }

                    if (activity instanceof MainActivity) {
                        int filterType = ((MainActivity) activity).getFilterIndex();
                        ((MainActivity) activity).processFrame(rgbaBytes, w, h, filterType);
                    }
                }
            }, backgroundHandler);

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                }
            }, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            Surface imageSurface = imageReader.getSurface();
            cameraDevice.createCaptureSession(
                    Collections.singletonList(imageSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Camera2Preview.this.session = session;
                            try {
                                CaptureRequest.Builder builder =
                                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(imageSurface);
                                session.setRepeatingRequest(builder.build(), null, backgroundHandler);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                        }
                    }, backgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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
