package com.seductive.tools.cameraholder.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
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

/**
 * Root view class which holds camera preview surface as child and reference for {@link ICameraHandler}
 * to stream data from camera to preview surface. While {@link CameraView#init()} is proceeded,
 * we create preview surface and camera handler depending on {@link Build.VERSION#SDK_INT}. If
 * current OS version is Marshmallow or higher (SDK_INT >= 21), the library can use camera2 API
 * with available focal length configuration. So, CameraView uses {@link AutoFitTextureView} as
 * preview surface view and {@link Camera2Handler} for making main actions with camera. Otherwise,
 * {@link CameraPreview} and {@link Camera1Handler} are used.
 * Also, the class provides public methods for opening/closing camera and taking single picture.
 * For correct usage, please, set your own {@link Settings} instance with pre-defined resolution,
 * camera type and focal length (only for camera2 API).
 * Also, to receive camera state updates and captured picture by camera, please, set
 * {@link CameraStateListener} and {@link CameraCaptureListener}.
 */
public class CameraView extends FrameLayout {

    private static final String TAG = CameraView.class.getSimpleName();

    private ICameraHandler mCameraHandler;
    private CameraStateListener mCameraStateListener;
    private CameraCaptureListener mCameraCaptureListener;

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

    /**
     * Creates concrete camera preview surface and {@link ICameraHandler} instances. After preview surface
     * is successfully created it's added into parent (FrameLayout).
     */
    private void init() {
        mConcreteCameraView = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? new AutoFitTextureView(getContext()) :
                new CameraPreview(getContext());
        mCameraHandler = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? new Camera2Handler(getContext()) :
                new Camera1Handler(getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        addView(mConcreteCameraView, 0, params);
    }

    /**
     * You will receive capture callback only if {@link this#mCameraCaptureListener} is not null
     */
    public void takePicture() {
        mCameraHandler.takePicture();
    }

    /**
     * Method for start opening camera and setting up all required {@link ICameraHandler} params.
     * You will receive camera state callback only if {@link this#mCameraStateListener} is not null.
     */
    public void setupCamera() {
        mCameraHandler.setup(mConcreteCameraView, mSettings, mCameraStateListener, mCameraCaptureListener);
        mCameraHandler.openCamera();
    }

    /**
     * Method for start closing camera and interrupting cleaning all inner {@link ICameraHandler}
     * inner tasks and callbacks;
     * You will receive camera state callback only if {@link this#mCameraStateListener} is not null.
     */
    public void releaseCamera() {
        mCameraHandler.closeCamera();
        mCameraHandler.release();
    }

    /**
     * Sets Settings to configure camera preview with.
     *
     * @param settings camera configuration settings.
     */
    public void setSettingsModel(Settings settings) {
        if (settings == null) {
            Log.w(TAG, "Custom settings are null, default settings will be used");
            this.mSettings = Settings.getDefault(getContext());
        } else {
            this.mSettings = settings;
        }
    }

    /**
     * Sets listener for camera events (open/close) and errors.
     *
     * @param cameraStateListener events listener
     */
    public void setCameraStateListener(CameraStateListener cameraStateListener) {
        this.mCameraStateListener = cameraStateListener;
    }

    /**
     * Sets listener for camera capture callbacks.
     *
     * @param cameraCaptureListener single image capture listener
     */
    public void setCameraCaptureListener(CameraCaptureListener cameraCaptureListener) {
        this.mCameraCaptureListener = cameraCaptureListener;
    }
}
