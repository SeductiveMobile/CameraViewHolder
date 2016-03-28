package com.seductive.tools.cameraholder.handler;

public interface CameraCaptureListener {

    void onSinglePreviewReceived(byte[] previewData);


    //TODO is redundant
    void onStreamPreviewReceived(byte[] previewData);
}
