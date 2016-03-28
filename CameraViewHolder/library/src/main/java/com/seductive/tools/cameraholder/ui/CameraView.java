package com.seductive.tools.cameraholder.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.seductive.tools.cameraholder.handler.CameraCaptureListener;
import com.seductive.tools.cameraholder.handler.CameraStateListener;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.handler.impl.Camera1Handler;
import com.seductive.tools.cameraholder.handler.impl.Camera2Handler;
import com.seductive.tools.cameraholder.model.Settings;
import com.seductive.tools.cameraholder.ui.camera1.CameraPreview;
import com.seductive.tools.cameraholder.ui.camera2.AutoFitTextureView;

public class CameraView extends FrameLayout {

    private ICameraHandler mCameraHandler;
    private CameraStateListener mCameraStateListener;
    private CameraCaptureListener mCameraCaptureListener;

    //TODO default settings
    private Settings mSettings;

    private View mConcreteCameraView;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mConcreteCameraView = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? new AutoFitTextureView(getContext()) :
                new CameraPreview(getContext());
        mCameraHandler = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? new Camera2Handler(getContext()) :
                new Camera1Handler(getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        addView(mConcreteCameraView, 0, params);
    }

    public void takePicture() {
        mCameraHandler.takePicture();
    }

    public void setupCamera() {
        mCameraHandler.setup(mConcreteCameraView, mSettings, mCameraStateListener, mCameraCaptureListener);
        mCameraHandler.openCamera();
    }

    public void releaseCamera() {
        mCameraHandler.closeCamera();
        mCameraHandler.release();
    }

    /**
     * @param settings
     */
    public void setSettingsModel(Settings settings) {
        this.mSettings = settings;
    }

    /**
     * @param cameraStateListener
     */
    public void setCameraStateListener(CameraStateListener cameraStateListener) {
        this.mCameraStateListener = cameraStateListener;
    }

    public void setCameraCaptureListener(CameraCaptureListener cameraCaptureListener) {
        this.mCameraCaptureListener = cameraCaptureListener;
    }
}
