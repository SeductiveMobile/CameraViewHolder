package com.seductive.tools.cameraholder.handler;

import android.view.View;

public interface ICameraHandler {

    enum CAMERA_STATE {
        IDLE, OPENING, OPEN, CLOSING, ERROR
    }

    /**
     * This method will init all UI dependencies and will save Listener reference. You should call release()
     * if you want to release Listener and UI references
     *
     * @param rootView
     * @param listener
     */
    void setup(View rootView, CameraStateListener listener);

    void openCamera();

    void closeCamera();

    /**
     * This method will release all references and resources.
     */
    void release();

    /**
     * For Walkthrough feature we need to provide focus parameter apart of settings SettingsModel
     *
     * @param focus
     */
    void setFocus(float focus);

    interface CameraStateListener {

        void onPreviewReceived(byte[] previewData);

        /**
         * This callback can be triggered on Main or Worker thread
         *
         * @param state
         */
        void onStateChanged(CAMERA_STATE state);
    }
}
