package com.seductive.tools.cameraholder.handler.impl;

import android.content.Context;
import android.hardware.Camera;
import android.view.View;

import com.seductive.tools.cameraholder.async.BaseAsyncTask;
import com.seductive.tools.cameraholder.handler.CameraCaptureListener;
import com.seductive.tools.cameraholder.handler.CameraStateListener;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.model.Settings;
import com.seductive.tools.cameraholder.ui.camera1.CameraPreview;
import com.seductive.tools.cameraholder.utils.CameraUtils;

/**
 * The Camera1Handler class is used for making the main actions with camera1 api hardware instance.
 *
 * @see Camera for deeper explanation of camera1 available options.
 */
public class Camera1Handler implements ICameraHandler, CameraPreview.OnPreviewUpdateListener {

    private Context mContext;

    /**
     * Resolution params that should be set from available ones.
     * See {@link CameraUtils#getSupportedVideoSizesForBackCamera()} and
     * {@link CameraUtils#getSupportedVideoSizesForFrontCamera()}
     */
    private int mResolutionWidth;
    private int mResolutionHeight;

    /**
     * Hardware camera id.
     * See {@link CameraUtils#getBackFacingCameraId()} and
     * {@link CameraUtils#getFrontFacingCameraId()}
     */
    private int mCameraId;

    private Camera mCamera;
    private CameraPreview mPreview;
    private OpenCameraTask mOpenCameraTask;
    private CameraStateListener mCameraStateListener;
    private CameraCaptureListener mCameraCaptureListener;
    private CAMERA_STATE mCurrCameraState = CAMERA_STATE.IDLE;
    private CAMERA_STATE mRequestedCameraState;

    private Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mCameraCaptureListener != null) {
                mCameraCaptureListener.onSinglePreviewReceived(data, CameraCaptureListener.Format.JPEG);
            }
            mCamera.startPreview();
        }
    };

    public Camera1Handler(Context context) {
        this.mContext = context;
    }

    @Override
    public void setup(View cameraView, Settings settings, CameraStateListener cameraStateListener,
                      CameraCaptureListener cameraCaptureListener) {

        mPreview = (CameraPreview) cameraView;

        mPreview.setPreviewParams(mResolutionWidth, mResolutionHeight);
        mPreview.setPreviewUpdateListener(this);
        mResolutionWidth = settings.getResolutionWidth();
        mResolutionHeight = settings.getResolutionHeight();

        this.mCameraId = settings.isCameraTypeBack() ?
                CameraUtils.getBackFacingCameraId() : CameraUtils.getFrontFacingCameraId();
        this.mCameraStateListener = cameraStateListener;
        this.mCameraCaptureListener = cameraCaptureListener;
        mRequestedCameraState = null;
    }

    @Override
    public void openCamera() {
        if (mCurrCameraState != CAMERA_STATE.IDLE) {
            mRequestedCameraState = CAMERA_STATE.OPEN;
            return;
        }
        cancelCameraTask();
        if (mCamera == null) {
            mOpenCameraTask = new OpenCameraTask();
            mOpenCameraTask.execute();
        }
    }

    @Override
    public void takePicture() {
        if (mCamera != null) {
            mCamera.takePicture(null, null, mJpegPictureCallback);
        }
    }

    @Override
    public void onUpdatePreview(byte[] previewData) {
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

    @Override
    public void release() {

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
        notifyListener();
    }

    /**
     * Sends camera state updates
     */
    private void notifyListener() {
        if (mCameraStateListener != null) {
            mCameraStateListener.onStateChanged(mCurrCameraState);
        }
    }

    /**
     * If handler started to open camera, method interrupted the process
     */
    private void cancelCameraTask() {
        if (mOpenCameraTask != null) {
            mOpenCameraTask.cancel(true);
            mOpenCameraTask = null;
        }
    }

    /**
     * Task for open hardware Camera in separate thread. If error while opening occurs,
     * task will notify handler by error state and will try to close camera.
     */
    private class OpenCameraTask extends BaseAsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            setCameraState(CAMERA_STATE.OPENING);
        }

        @Override
        public void onResult(Void aVoid) {
            setCameraState(CAMERA_STATE.OPEN);
        }

        @Override
        public void onException(Exception e) {
            setCameraState(CAMERA_STATE.ERROR);
            if (mCamera != null) {
                closeCamera();
            }
        }

        @Override
        public Void performInBackground(Void... taskParams) throws Exception {
            mCamera = Camera.open(mCameraId);
            Camera.Parameters params = mCamera.getParameters();
            mCamera.setPreviewCallback(mPreview);
            mCamera.setDisplayOrientation(90);
            if (mResolutionWidth == 0 || mResolutionHeight == 0) {
                setCameraState(CAMERA_STATE.ERROR);
            } else {
                params.setPreviewSize(mResolutionWidth, mResolutionHeight);
                mCamera.setParameters(params);

                mPreview.refreshCamera(mCamera);
            }
            return null;
        }
    }

    /**
     * Asynchronously closes camera.
     */
    private class CameraCloseRunnable implements Runnable {
        @Override
        public void run() {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }
            setCameraState(CAMERA_STATE.IDLE);
        }
    }
}
