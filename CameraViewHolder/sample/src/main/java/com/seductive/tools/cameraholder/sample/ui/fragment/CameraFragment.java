package com.seductive.tools.cameraholder.sample.ui.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seductive.tools.cameraholder.async.BaseAsyncTask;
import com.seductive.tools.cameraholder.handler.CameraCaptureListener;
import com.seductive.tools.cameraholder.handler.CameraStateListener;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.model.Settings;
import com.seductive.tools.cameraholder.sample.R;
import com.seductive.tools.cameraholder.sample.databinding.FragmentCameraBinding;
import com.seductive.tools.cameraholder.sample.ui.dialog.ProgressDialogFragment;
import com.seductive.tools.cameraholder.sample.utils.SharedPreferencesUtil;
import com.seductive.tools.cameraholder.sample.utils.UIUtils;
import com.seductive.tools.cameraholder.ui.CameraView;
import com.seductive.tools.cameraholder.utils.CameraUtils;

import java.io.ByteArrayInputStream;

public class CameraFragment extends Fragment {

    private CameraView mCameraView;
    private Settings mSettings;
    private CameraStateListener mCameraStateListener = new CameraStateListener() {

        @Override
        public void onStateChanged(ICameraHandler.CAMERA_STATE state) {
            /**
             * Depending on state method allows user to operate through updated camera states.
             * {@link com.seductive.tools.cameraholder.handler.ICameraHandler.CAMERA_STATE}
             */
        }
    };

    private CameraCaptureListener mCameraCaptureListener = new CameraCaptureListener() {
        @Override
        public void onSinglePreviewReceived(byte[] previewData, Format format) {
            showImageDueToFormat(previewData, format);
        }
    };

    private FragmentCameraBinding mBinding;
    private ConvertYUVToBitmapTask mConvertYUVToBitmapTask;

    public static CameraFragment createInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false);

        mCameraView = (CameraView) mBinding.getRoot().findViewById(R.id.camera_view);
        mSettings = SharedPreferencesUtil.getLastCachedSettings(getContext());
        mCameraView.setSettingsModel(mSettings);
        mCameraView.setCameraStateListener(mCameraStateListener);
        mCameraView.setCameraCaptureListener(mCameraCaptureListener);

        mBinding.takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.takePicture();
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.setupCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.releaseCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConvertYUVToBitmapTask != null) {
            mConvertYUVToBitmapTask.cancel(true);
        }
    }

    private void showImageDueToFormat(byte[] previewData, CameraCaptureListener.Format format) {
        if (format.equals(CameraCaptureListener.Format.JPEG)) {
            ByteArrayInputStream imageStream = new ByteArrayInputStream(previewData);
            Bitmap landscapeBitmap = BitmapFactory.decodeStream(imageStream);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap portraitBitmap = Bitmap.createBitmap(landscapeBitmap, 0, 0,
                    landscapeBitmap.getWidth(), landscapeBitmap.getHeight(), matrix, true);

            ProgressDialogFragment instance = ProgressDialogFragment.newInstance(portraitBitmap);
            instance.show(getFragmentManager(), ProgressDialogFragment.TAG);
        } else {
            mConvertYUVToBitmapTask = new ConvertYUVToBitmapTask(previewData);
            mConvertYUVToBitmapTask.execute();
        }
    }

    private void showLoader() {
        mBinding.setLoading(true);
    }

    private void hideLoader() {
        mBinding.setLoading(false);
    }

    private class ConvertYUVToBitmapTask extends BaseAsyncTask<Void, Void, Bitmap> {

        private byte[] yuvData;

        ConvertYUVToBitmapTask(byte[] data) {
            this.yuvData = data;
        }

        @Override
        public void onPreExecute() {
            showLoader();
            UIUtils.showToastMsg(getContext(), "Start converting YUV image...");
        }

        @Override
        public void onResult(Bitmap resultBitmap) {
            hideLoader();
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap portraitBitmap = Bitmap.createBitmap(resultBitmap, 0, 0,
                    resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);

            ProgressDialogFragment instance = ProgressDialogFragment.newInstance(portraitBitmap);
            instance.show(getFragmentManager(), ProgressDialogFragment.TAG);
        }

        @Override
        public void onException(Exception e) {
            hideLoader();
            UIUtils.showToastMsg(getContext(), "Unable to convert YUV image into bitmap");
        }

        @Override
        public Bitmap performInBackground(Void... params) throws Exception {
            return CameraUtils.convertYUV(yuvData, mSettings.getResolutionWidth(),
                    mSettings.getResolutionHeight(), null);
        }
    }
}
