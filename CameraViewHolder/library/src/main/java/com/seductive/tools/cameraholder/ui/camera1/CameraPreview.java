package com.seductive.tools.cameraholder.ui.camera1;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.seductive.tools.cameraholder.utils.UIUtils;

import java.io.IOException;

/**
 * Provides extended drawing surface with custom listener for receiving capture result with array
 * of bites. CameraPreview uses deprecated android.hardware.Camera and
 * android.hardware.Camera.PreviewCallback.
 * Preview frame is resized according to chosen resolution. If no resolution is chosen,
 * we use screen params.
 *
 * @see SurfaceView
 * @see android.view.SurfaceHolder.Callback
 * @see android.hardware.Camera.PreviewCallback
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private SurfaceHolder mHolder;

    /**
     * Requested camera instance
     */
    private Camera mCamera;

    /**
     * Listener to transfer captured frame into subscribers
     *
     * @see com.seductive.tools.cameraholder.ui.camera1.CameraPreview.OnPreviewUpdateListener
     * @see #onPreviewFrame(byte[], Camera)
     */
    private OnPreviewUpdateListener previewUpdateListener;

    /**
     * Relative horizontal and vertical sizes
     */
    private int mPreviewWidth, mPreviewHeight;

    public CameraPreview(Context context) {
        this(context, 0, 0);
    }

    public CameraPreview(Context context, int previewWidth, int previewHeight) {
        super(context);
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mPreviewWidth = previewWidth == 0 ? UIUtils.getScreenWidth(context) : previewWidth;
        this.mPreviewHeight = previewHeight == 0 ? UIUtils.getScreenHeight(context) : previewHeight;
    }

    /**
     * Called as preview frames are displayed
     *
     * @param data   the contents of the preview frame in the format defined
     *               by {@link android.graphics.ImageFormat}
     * @param camera the Camera instance
     * @see android.hardware.Camera.PreviewCallback
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (previewUpdateListener != null) {
            previewUpdateListener.onUpdatePreview(data);
        }
    }

    /**
     * Method is called immediately after the surface is first created.
     * If camera instance is already set, we can start preview.
     *
     * @param holder SurfaceHolder instance
     * @see android.view.SurfaceHolder.Callback
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop current preview and re-set camera instance in case of changing current camera
     * params or Camera instance
     *
     * @param camera Camera instance
     * @see Camera
     */
    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If your preview can change or rotate, take care of those events here.
     * Make sure to stop the preview before resizing or reformatting it.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param w      The new width of the surface.
     * @param h      The new height of the surface.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        refreshCamera(mCamera);
    }

    /**
     * Method to set a camera instance
     *
     * @param camera Camera instance
     * @see Camera
     */
    public void setCamera(Camera camera) {
        mCamera = camera;
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    /**
     * Method to set OnPreviewUpdateListener instance
     *
     * @param previewUpdateListener listener instance
     * @return CameraPreview instance
     */
    public CameraPreview setPreviewUpdateListener(OnPreviewUpdateListener previewUpdateListener) {
        this.previewUpdateListener = previewUpdateListener;
        return this;
    }

    /**
     * Custom listener which provides method for transferring camera capture result into subscribers
     * as byte array.
     */
    public interface OnPreviewUpdateListener {
        void onUpdatePreview(byte[] previewData);
    }

    /**
     * Sets width and height to view according to available mPreviewWidth and mPreviewHeight.
     * According to method usage view will be resized with saving proportion between
     * width and heigth.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     *                          The requirements are encoded with
     *                          {@link android.view.View.MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                          The requirements are encoded with
     *                          {@link android.view.View.MeasureSpec}.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int actualWidth = UIUtils.getScreenWidth(getContext());
        final int actualHeight = UIUtils.getScreenHeight(getContext());

        if (mPreviewWidth > 0 && mPreviewHeight > 0) {
            float widthRatio = (float) actualWidth / mPreviewWidth;
            float heightRatio = (float) actualHeight / mPreviewHeight;

            float maxRatio = Math.min(widthRatio, heightRatio);

            int scaledWidth = (int) (mPreviewWidth * maxRatio);
            int scaledHeight = (int) (mPreviewHeight * maxRatio);
            setMeasuredDimension(scaledWidth, scaledHeight);
        }
    }
}