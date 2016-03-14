package com.seductive.tools.cameraholder.sample.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.util.Size;

import com.seductive.tools.cameraholder.sample.event.InitializationEvent;
import com.seductive.tools.cameraholder.sample.utils.SharedPreferencesUtil;
import com.seductive.tools.cameraholder.utils.CameraUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class InitializationService extends IntentService {

    public InitializationService() {
        super("SettingsInitializationService");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onHandleIntent(Intent intent) {
        if (SharedPreferencesUtil.getBackFacingCameraResolutions(this).isEmpty() &&
                SharedPreferencesUtil.getFrontFacingCameraResolutions(this).isEmpty()) {

            List<String> backFaceCameraResolutions = new ArrayList<>();
            List<String> frontFaceCameraResolutions = new ArrayList<>();
            int backCameraId = CameraUtils.getBackFacingCameraId();
            boolean backCameraFocusAvailable = false;
            if (backCameraId != -1) {
                backCameraFocusAvailable = CameraUtils.isCameraFocusAvailable(getBaseContext(), backCameraId);
                SharedPreferencesUtil.setBackCameraFocusAvailable(this, backCameraFocusAvailable);
            }
            if (!backCameraFocusAvailable) {
                List<Camera.Size> backCameraSizes = CameraUtils.getSupportedVideoSizesForBackCamera();
                for (Camera.Size size : backCameraSizes) {
                    backFaceCameraResolutions.add(size.width + "x" + size.height);
                }
            } else {
                List<Size> sizes = CameraUtils.getSupportedOutputSizesForBackCamera2(getBaseContext());
                for (Size size : sizes) {
                    backFaceCameraResolutions.add(size.toString());
                }
            }
            int frontCameraId = CameraUtils.getBackFacingCameraId();
            boolean frontCameraFocusAvailable = false;
            if (frontCameraId != -1) {
                frontCameraFocusAvailable = CameraUtils.isCameraFocusAvailable(getBaseContext(), frontCameraId);
                SharedPreferencesUtil.setBackCameraFocusAvailable(getBaseContext(), frontCameraFocusAvailable);
            }
            if (!frontCameraFocusAvailable) {
                List<Camera.Size> frontCameraSizes = CameraUtils.getSupportedVideoSizesForFrontCamera();

                for (Camera.Size size : frontCameraSizes) {
                    frontFaceCameraResolutions.add(size.width + "x" + size.height);
                }
            } else {
                List<Size> sizes = CameraUtils.getSupportedOutputSizesForFrontCamera2(getBaseContext());
                for (Size size : sizes) {
                    frontFaceCameraResolutions.add(size.toString());
                }
            }

            SharedPreferencesUtil.setBackFacingCameraResolutions(this, backFaceCameraResolutions);
            SharedPreferencesUtil.setFrontFacingCameraResolutions(this, frontFaceCameraResolutions);
        }
        EventBus.getDefault().postSticky(new InitializationEvent());
    }
}