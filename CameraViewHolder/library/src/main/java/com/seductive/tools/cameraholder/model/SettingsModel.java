package com.seductive.tools.cameraholder.model;

public class SettingsModel {

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

    public float getFocus() {
        return focus;
    }

    public SettingsModel setFocus(float focus) {
        this.focus = focus;
        return this;
    }

    public CAMERA_TYPE getCameraType() {
        return cameraType;
    }

    public SettingsModel setCameraType(CAMERA_TYPE cameraType) {
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

    public SettingsModel setBackCameraFocusAvailable(boolean backCameraFocusAvailable) {
        this.backCameraFocusAvailable = backCameraFocusAvailable;
        return this;
    }

    public boolean isFrontCameraFocusAvailable() {
        return frontCameraFocusAvailable;
    }

    public SettingsModel setFrontCameraFocusAvailable(boolean frontCameraFocusAvailable) {
        this.frontCameraFocusAvailable = frontCameraFocusAvailable;
        return this;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public SettingsModel setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
        return this;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public SettingsModel setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
        return this;
    }
}
