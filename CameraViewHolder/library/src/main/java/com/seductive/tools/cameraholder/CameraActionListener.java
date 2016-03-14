package com.seductive.tools.cameraholder;

public interface CameraActionListener {

    void onCameraOpened();

    void onCameraClosed();

    //TODO add preview format
    void onPreviewCaptured();
}
