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
import com.seductive.tools.cameraholder.utils.UIUtils;

public class Camera1Handler implements ICameraHandler, CameraPreview.OnPreviewUpdateListener {

    private Context mContext;

    private int mResolutionWidth;
    private int mResolutionHeight;
    private int mCameraId;
    private Camera mCamera;
    private CameraPreview mPreview;
    private OpenCameraTask mOpenCameraTask;
    private CameraStateListener mCameraStateListener;
    private CameraCaptureListener mCameraCaptureListener;
    private CAMERA_STATE mCurrCameraState = CAMERA_STATE.IDLE;
    private CAMERA_STATE mRequestedCameraState;

    private Camera.PictureCallback mJpegPictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mCameraCaptureListener != null) {
                mCameraCaptureListener.onSinglePreviewReceived(data);
            }
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
        //TODO validate preview size
        if (settings == null) {
            mResolutionWidth = UIUtils.getScreenWidth(mContext);
            mResolutionHeight = UIUtils.getScreenHeight(mContext);
        } else {
            mResolutionWidth = settings.getResolutionWidth();
            mResolutionHeight = settings.getResolutionHeight();
        }

        this.mCameraId = settings == null || settings.isCameraTypeBack() ?
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
        if (mCameraCaptureListener != null) {
            mCameraCaptureListener.onStreamPreviewReceived(previewData);
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

    @Override
    public void release() {

    }

    @Override
    public void setFocus(float focus) {
        //stub
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

    private void notifyListener() {
        if (mCameraStateListener != null)
            mCameraStateListener.onStateChanged(mCurrCameraState);
    }

    private void cancelCameraTask() {
        if (mOpenCameraTask != null) {
            mOpenCameraTask.cancel(true);
            mOpenCameraTask = null;
        }
    }

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
