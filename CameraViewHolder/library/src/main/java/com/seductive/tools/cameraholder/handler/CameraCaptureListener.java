package com.seductive.tools.cameraholder.handler;

import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;

public interface CameraCaptureListener {

    /**
     * Provides currently supported formats for camera1 API
     * ({@link com.seductive.tools.cameraholder.handler.CameraCaptureListener.Format#JPEG})
     * and camera2 API
     * ({@link com.seductive.tools.cameraholder.handler.CameraCaptureListener.Format#YUV}).
     */
    enum Format {
        JPEG, YUV
    }

    /**
     * Method is used for receiving jpeg image in byte array requested by method capture().
     * For camera1 API capture is requested by
     * {@link android.hardware.Camera#takePicture(Camera.ShutterCallback, Camera.PictureCallback, Camera.PictureCallback)}
     * and for camera2 API is used
     * {@link android.hardware.camera2.CameraCaptureSession.CaptureCallback#capture(CaptureRequest, CameraCaptureSession.CaptureCallback, Handler)}
     * insted.
     *
     * @param previewData captured jpeg frame
     */
    void onSinglePreviewReceived(byte[] previewData, Format format);
}
