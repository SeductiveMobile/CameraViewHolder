package com.seductive.tools.cameraholder.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.seductive.tools.cameraholder.CameraActionListener;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.handler.impl.Camera1Handler;
import com.seductive.tools.cameraholder.handler.impl.Camera2Handler;
import com.seductive.tools.cameraholder.model.SettingsModel;
import com.seductive.tools.cameraholder.ui.camera1.CameraPreview;
import com.seductive.tools.cameraholder.ui.camera2.AutoFitTextureView;
import com.seductive.tools.cameraholder.utils.CameraUtils;

public class CameraView extends FrameLayout {

    private Context mContext;

    private ICameraHandler mCameraHandler;
    private CameraActionListener mCameraActionListener;
    private SettingsModel mSettingsModel;


    public CameraView(Context context) {
        super(context);
        setup();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    private void setup() {
        mSettingsModel = new SettingsModel();
        createPreview();
        createHandler();
    }

    private void createPreview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (mSettingsModel.isCameraTypeBack() && mSettingsModel.isBackCameraFocusAvailable() ||
                        mSettingsModel.isCameraTypeFront())) {
            addView(new AutoFitTextureView(getContext()));
        } else {
            addView(new CameraPreview(getContext(), 1080, 1920));
        }
    }

    private void createHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (mSettingsModel.isCameraTypeBack() && mSettingsModel.isBackCameraFocusAvailable() ||
                        mSettingsModel.isCameraTypeFront())) {
            mCameraHandler = new Camera2Handler(getContext(), String.valueOf(mSettingsModel.isCameraTypeBack() ?
                    CameraUtils.getBackFacingCameraId() : CameraUtils.getFrontFacingCameraId()), mSettingsModel);
        } else {
            mCameraHandler = new Camera1Handler(mSettingsModel.isCameraTypeBack() ?
                    CameraUtils.getBackFacingCameraId() : CameraUtils.getFrontFacingCameraId(), mSettingsModel);
        }
    }

    public void setSettingsModel(SettingsModel settingsModel) {
        this.mSettingsModel = settingsModel;
        //TODO
        setup();
    }

    public void setCameraActionListener(CameraActionListener cameraActionListener) {
        this.mCameraActionListener = cameraActionListener;
    }
}
