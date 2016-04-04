package com.seductive.tools.cameraholder.model;

import android.content.Context;

import com.seductive.tools.cameraholder.utils.UIUtils;

/**
 * Class provides parameters for configuration both camera1 and camera2.
 * <p>
 * Warning:
 * 1. focal length {@link Settings#focus} could be used only for camera2 and it's ignored
 * otherwise;
 * 2. Resolution params ({@link Settings#resolution} or {@link Settings#resolutionWidth} and
 * {@link Settings#resolutionHeight}) must be set only after receiving results from
 * {@link com.seductive.tools.cameraholder.utils.CameraUtils} methods for getting available
 * resolution sizes. Preview option is limited by these default values and camera could be opened
 * correctly only if preview params are exists in inner resolution sizes list.
 */
public class Settings {

    public enum CAMERA_TYPE {
        BACK("Back"),
        FRONT("Front");

        private String description;

        CAMERA_TYPE(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private CAMERA_TYPE cameraType = CAMERA_TYPE.BACK;
    private String resolution;
    private float focus;

    private int resolutionHeight;
    private int resolutionWidth;
    private boolean backCameraFocusAvailable;
    private boolean frontCameraFocusAvailable;

    /**
     * Method provides default instance if user didn't set custom one.
     *
     * @param context
     * @return
     */
    public static Settings getDefault(Context context) {
        Settings settings = new Settings();
        settings.backCameraFocusAvailable = false;
        settings.frontCameraFocusAvailable = false;
        settings.cameraType = CAMERA_TYPE.BACK;
        settings.resolutionHeight = UIUtils.getScreenHeight(context);
        settings.resolutionWidth = UIUtils.getScreenWidth(context);

        return settings;
    }

    public float getFocus() {
        return focus;
    }

    public Settings setFocus(float focus) {
        this.focus = focus;
        return this;
    }

    public CAMERA_TYPE getCameraType() {
        return cameraType;
    }

    public Settings setCameraType(CAMERA_TYPE cameraType) {
        this.cameraType = cameraType;
        return this;
    }

    public boolean isCameraTypeFront() {
        return this.cameraType == CAMERA_TYPE.FRONT;
    }

    public boolean isCameraTypeBack() {
        return this.cameraType == CAMERA_TYPE.BACK;
    }

    public boolean isBackCameraFocusAvailable() {
        return backCameraFocusAvailable;
    }

    public Settings setBackCameraFocusAvailable(boolean backCameraFocusAvailable) {
        this.backCameraFocusAvailable = backCameraFocusAvailable;
        return this;
    }

    public boolean isFrontCameraFocusAvailable() {
        return frontCameraFocusAvailable;
    }

    public Settings setFrontCameraFocusAvailable(boolean frontCameraFocusAvailable) {
        this.frontCameraFocusAvailable = frontCameraFocusAvailable;
        return this;
    }

    public Settings setResolution(String resolution) {
        this.resolution = resolution;
        return this;
    }

    public String getResolution() {
        return resolution;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public Settings setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
        return this;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public Settings setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
        return this;
    }
}
