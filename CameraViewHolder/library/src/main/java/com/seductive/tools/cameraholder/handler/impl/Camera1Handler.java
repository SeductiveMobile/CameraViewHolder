package com.seductive.tools.cameraholder.handler.impl;

import android.hardware.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.seductive.tools.cameraholder.R;
import com.seductive.tools.cameraholder.model.SettingsModel;
import com.seductive.tools.cameraholder.async.BaseAsyncTask;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.ui.camera1.CameraPreview;

public class Camera1Handler implements ICameraHandler, CameraPreview.OnPreviewUpdateListener {

    private int mResolutionWidth;
    private int mResolutionHeight;
    private int mCameraId;
    private Camera mCamera;
    private CameraPreview mPreview;
    private OpenCameraTask mOpenCameraTask;
    private CameraStateListener cameraStateListener;
    private CAMERA_STATE mCurrCameraState = CAMERA_STATE.IDLE;
    private CAMERA_STATE mRequestedCameraState;

    public Camera1Handler(int cameraId, SettingsModel settingsModel) {
        //TODO
//        mResolutionWidth = settingsModel.getResolutionWidth();
//        mResolutionHeight = settingsModel.getResolutionHeight();
        mResolutionWidth = 1080;
        mResolutionHeight = 1920;
        this.mCameraId = cameraId;
    }

    @Override
    public void setup(View rootView, CameraStateListener listener) {
        cameraStateListener = listener;
        mPreview = new CameraPreview(rootView.getContext(), mResolutionWidth, mResolutionHeight).setPreviewUpdateListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        ((RelativeLayout) rootView.findViewById(R.id.camera_preview)).addView(mPreview, 0, params);
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
    public void onUpdatePreview(byte[] previewData) {
        if (cameraStateListener != null) {
            cameraStateListener.onPreviewReceived(previewData);
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
        if (cameraStateListener != null)
            cameraStateListener.onStateChanged(mCurrCameraState);
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
