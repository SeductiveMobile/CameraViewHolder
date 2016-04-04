package com.seductive.tools.cameraholder.handler;

/**
 * Provides method for checking all camera states while making the main actions with is - opening/closing.
 * States are declared in {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE}.
 * Default camera state on the start is {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#IDLE}.
 * After handler starts its inner async camera opening task, the state changes to
 * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#OPENING}.
 * In normal execution without errors while opening state will be changed to
 * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#OPEN}.
 * If hardware camera is busy by other process and could not be opened, state will be changed to
 * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#ERROR}. Camera could
 * be busy by other process if other app opened it but didn't close correctly.
 * After receiving error handler tries to close camera. Also, handler closes camera when controller
 * changes its inner state to stop/destroy (onStop()/onDestroy() methods from Activity or Fragment).
 * Both cases change state to
 * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#CLOSING}.
 * After camera was successfully closed, state is changed on initial
 * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE#IDLE}.
 * <p>
 * The same method and the same states are used for both camera APIs.
 */
public interface CameraStateListener {

    /**
     * This callback can be triggered on Main or Worker thread
     *
     * @param state current camera state. Is independent from camera API, callbacks are the same
     *              for both camera versions.
     */
    void onStateChanged(ICameraHandler.CAMERA_STATE state);
}
