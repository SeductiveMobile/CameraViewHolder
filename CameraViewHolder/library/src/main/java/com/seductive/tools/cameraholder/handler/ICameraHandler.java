package com.seductive.tools.cameraholder.handler;

import android.view.View;

import com.seductive.tools.cameraholder.model.Settings;

/**
 * Provides methods for main actions with camera1 and camera2. In current library version
 * 2 implementations are available: {@link com.seductive.tools.cameraholder.handler.impl.Camera1Handler}
 * and {@link com.seductive.tools.cameraholder.handler.impl.Camera2Handler}. Both implementations
 * must to hold references to camera preview due to ICameraHandler purpose: camera preview size,
 * resolution and focal length configurations. Note, that you can configure focal length only using
 * camera2 view and handler implementations. If {@link Settings} instance is not provided, handler will use
 * default settings described in link above. You may provide {@link CameraStateListener} and
 * {@link CameraCaptureListener} but it's not required for correct camera work.
 */
public interface ICameraHandler {

    /**
     * IDLE - camera is available for main actions, but not opened
     * OPENING - camera opening task is in progress
     * OPEN - successfully opened
     * CLOSING - camera closing task is in progress
     * ERROR - error was captured while opening camera
     */
    enum CAMERA_STATE {
        IDLE, OPENING, OPEN, CLOSING, ERROR
    }

    /**
     * Method for applying setting to camera preview, initializing internal handler settings,
     * saving references to {@link CameraStateListener} and {@link CameraCaptureListener}
     * You should call release() to destroying this references.
     *
     * @param cameraView            instance of camera preview surface view for drawing stream of
     *                              captured frames. In current implementation 2 instances are available:
     *                              {@link com.seductive.tools.cameraholder.ui.camera1.CameraPreview}
     *                              for camera1 API
     *                              and {@link com.seductive.tools.cameraholder.ui.camera2.AutoFitTextureView}
     *                              for camera2 API.
     * @param settings              {@link Settings} instance provides info about required camera id,
     *                              resolution and focal length if available (only for camera2 API)
     * @param cameraStateListener   {@link CameraStateListener} provides callbacks for camera open/close
     *                              events and
     *                              {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE}
     *                              events. Controller component which uses
     *                              {@link com.seductive.tools.cameraholder.ui.CameraView} should use this events for
     *                              receiving success/failure statuses
     * @param cameraCaptureListener {@link CameraCaptureListener} provides callbacks for single captured
     *                              frame
     */
    void setup(View cameraView, Settings settings,
               CameraStateListener cameraStateListener, CameraCaptureListener cameraCaptureListener);

    /**
     * Method for setting up camera preview into camera and asynchronous opening camera. If
     * camera is opened successfully and {@link CameraStateListener} is provided,
     * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#OPEN} state will
     * be received
     */
    void openCamera();

    /**
     * If {@link CameraCaptureListener} is provided, method triggers receiving single captured frame.
     */
    void takePicture();

    /**
     * Methor for asynchronous closing camera.
     */
    void closeCamera();

    /**
     * This method will release all references and resources.
     */
    void release();
}
