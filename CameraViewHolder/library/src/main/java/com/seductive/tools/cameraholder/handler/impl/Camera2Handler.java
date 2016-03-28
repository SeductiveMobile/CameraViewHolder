package com.seductive.tools.cameraholder.handler.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import com.seductive.tools.cameraholder.async.BaseAsyncTask;
import com.seductive.tools.cameraholder.handler.CameraCaptureListener;
import com.seductive.tools.cameraholder.handler.CameraStateListener;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.model.Settings;
import com.seductive.tools.cameraholder.ui.camera2.AutoFitTextureView;
import com.seductive.tools.cameraholder.utils.CameraUtils;
import com.seductive.tools.cameraholder.utils.UIUtils;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Handler implements ICameraHandler {

    private boolean mFlashSupported;
    private String mCameraId;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private Size mPreviewSize;
    private float mCameraFocus;
    private OpenCameraTask mOpenCameraTask;
    private AutoFitTextureView mCameraPreview;

    private Context mContext;
    private CameraStateListener mCameraStateListener;
    private CameraCaptureListener mCameraCaptureListener;
    private CAMERA_STATE mCurrCameraState = CAMERA_STATE.IDLE;
    private CAMERA_STATE mRequestedCameraState;
    private Settings settings;
    private float mFocus;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            setupOutputAndCreateCameraTask();
            configureTransform(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
            Log.d("TAG", "onCaptureCompleted");
        }
    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            setCameraState(CAMERA_STATE.ERROR);
        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = null;
            img = reader.acquireLatestImage();
            try {
                if (img == null) throw new NullPointerException("cannot be null");
                Image.Plane Y = img.getPlanes()[0];
                Image.Plane U = img.getPlanes()[1];
                Image.Plane V = img.getPlanes()[2];

                int Yb = Y.getBuffer().remaining();
                int Ub = U.getBuffer().remaining();
                int Vb = V.getBuffer().remaining();

                byte[] data = new byte[Yb + Ub + Vb];

                Y.getBuffer().get(data, 0, Yb);
                U.getBuffer().get(data, Yb, Ub);
                V.getBuffer().get(data, Yb + Ub, Vb);
                if (mCameraCaptureListener != null) {
                    mCameraCaptureListener.onStreamPreviewReceived(data);
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            } finally {
                if (img != null) {
                    img.close();
                }
            }
        }
    };

    public Camera2Handler(Context context) {
        this.mContext = context;
    }

    @Override
    public void setup(View cameraView, Settings settings, CameraStateListener cameraStateListener,
                      CameraCaptureListener cameraCaptureListener) {
        this.mCameraPreview = (AutoFitTextureView) cameraView;
        this.settings = settings;
        this.mCameraId = settings.isCameraTypeBack() ? String.valueOf(CameraUtils.getBackFacingCameraId()) :
                String.valueOf(CameraUtils.getFrontFacingCameraId());
        this.mCameraStateListener = cameraStateListener;
        this.mCameraCaptureListener = cameraCaptureListener;
        this.mRequestedCameraState = null;
        this.mFocus = settings.getFocus();
    }

    @Override
    public void release() {
        mCameraPreview = null;
        mCameraStateListener = null;
        mCameraCaptureListener = null;
    }

    @Override
    public void setFocus(float focus) {
        mFocus = focus;
    }

    @Override
    public void openCamera() {
        if (mCurrCameraState != CAMERA_STATE.IDLE) {
            mRequestedCameraState = CAMERA_STATE.OPEN;
            return;
        }
        setCameraState(CAMERA_STATE.OPENING);

        if (mCameraPreview == null) {
            return;
        }
        if (mCameraPreview.isAvailable()) {
            setupOutputAndCreateCameraTask();
        } else {
            mCameraPreview.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void takePicture() {
        if (mCaptureSession != null) {
            try {
                mCaptureSession.capture(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeCamera() {
        if (mCurrCameraState != CAMERA_STATE.OPEN) {
            mRequestedCameraState = CAMERA_STATE.IDLE;
            return;
        }
        setCameraState(CAMERA_STATE.CLOSING);
        cancelCameraTask();
        new Thread(new CameraCloseRunnable()).start();
    }

    private void setupOutputAndCreateCameraTask() {
        if (mBackgroundHandler == null) {
            startBackgroundThread();
        }
        setUpCameraOutputs();
        mOpenCameraTask = new OpenCameraTask();
        mOpenCameraTask.execute();
    }

    private void cancelCameraTask() {
        if (mOpenCameraTask != null) {
            mOpenCameraTask.cancel(true);
            mOpenCameraTask = null;
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mCameraPreview.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface(), surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_OFF);
                                if (settings.isCameraTypeBack() && settings.isBackCameraFocusAvailable() ||
                                        settings.isCameraTypeFront() && settings.isFrontCameraFocusAvailable()) {
                                    mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mCameraFocus);
                                }
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            UIUtils.showToastMsg(mContext, "Configuration failed");
                        }
                    }, null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    private void setUpCameraOutputs() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                if (!cameraId.equalsIgnoreCase(mCameraId)) {
                    continue;
                }
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                mPreviewSize = new Size(settings.getResolutionWidth(), settings.getResolutionHeight());

                // For still image captures, we use the largest available size.
                mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                        ImageFormat.YUV_420_888, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = mContext.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mCameraPreview.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mCameraPreview.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                if (settings.isCameraTypeBack() && settings.isBackCameraFocusAvailable() ||
                        settings.isCameraTypeFront() && settings.isFrontCameraFocusAvailable()) {
                    Object minFocusDistanceObj = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                    float minFocusDistance = minFocusDistanceObj != null ? (float) minFocusDistanceObj : 1f;

                    mCameraFocus = minFocusDistance - minFocusDistance * mFocus;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            UIUtils.showToastMsg(mContext, "NPE");
        }
    }

    private void setCameraState(CAMERA_STATE newState) {
        if (mCurrCameraState == newState)
            return;
        mCurrCameraState = newState;
        if (mRequestedCameraState != null &&
                mRequestedCameraState != mCurrCameraState &&
                (mCurrCameraState != CAMERA_STATE.OPENING &&
                        mCurrCameraState != CAMERA_STATE.CLOSING)) {
            switch (mRequestedCameraState) {
                case OPEN:
                    openCamera();
                    break;
                case IDLE:
                    closeCamera();
                    break;
            }
            mRequestedCameraState = null;
            return;
        }
        notifyListenerStateChanged();
    }

    private void notifyListenerStateChanged() {
        if (mCameraStateListener != null) {
            mCameraStateListener.onStateChanged(mCurrCameraState);
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mPreviewSize || null == mCameraPreview) {
            return;
        }
        int rotation = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            float sxScale = (float) viewHeight / mPreviewSize.getHeight();
            float syScale = (float) viewWidth / mPreviewSize.getWidth();
            matrix.postScale(sxScale, syScale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mCameraPreview.setTransform(matrix);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class OpenCameraTask extends BaseAsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
        }

        @Override
        public void onResult(Void aVoid) {
            setCameraState(CAMERA_STATE.OPEN);
        }

        @Override
        public void onException(Exception e) {
            setCameraState(CAMERA_STATE.ERROR);
            closeCamera();
        }

        @Override
        public Void performInBackground(Void... taskParams) throws Exception {
            try {
                if (!mCameraOpenCloseLock.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class CameraCloseRunnable implements Runnable {

        @Override
        public void run() {
            try {
                mCameraOpenCloseLock.acquire();
                if (null != mCaptureSession) {
                    mCaptureSession.abortCaptures();
                    mCaptureSession.close();
                    mCaptureSession = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                if (null != mImageReader) {
                    mImageReader.close();
                    mImageReader = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mCameraOpenCloseLock.release();
            }
            stopBackgroundThread();
            setCameraState(ICameraHandler.CAMERA_STATE.IDLE);
        }
    }
}

