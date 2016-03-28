package com.seductive.tools.cameraholder.handler;

public interface CameraStateListener {

    /**
     * TODO receive status
     */
    void onCameraOpened();

    /**
     * TODO receive status
     */
    void onCameraClosed();

    /**
     * This callback can be triggered on Main or Worker thread
     *
     * @param state
     */
    void onStateChanged(ICameraHandler.CAMERA_STATE state);
}
