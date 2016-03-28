package com.seductive.tools.cameraholder.sample.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seductive.tools.cameraholder.handler.CameraCaptureListener;
import com.seductive.tools.cameraholder.handler.CameraStateListener;
import com.seductive.tools.cameraholder.handler.ICameraHandler;
import com.seductive.tools.cameraholder.sample.R;
import com.seductive.tools.cameraholder.sample.databinding.FragmentCameraBinding;
import com.seductive.tools.cameraholder.sample.utils.SharedPreferencesUtil;
import com.seductive.tools.cameraholder.ui.CameraView;

public class CameraFragment extends Fragment {

    private CameraView mCameraView;
    private CameraStateListener mCameraStateListener = new CameraStateListener() {
        @Override
        public void onCameraOpened() {

        }

        @Override
        public void onCameraClosed() {

        }

        @Override
        public void onStateChanged(ICameraHandler.CAMERA_STATE state) {

        }
    };

    private CameraCaptureListener mCameraCaptureListener = new CameraCaptureListener() {
        @Override
        public void onSinglePreviewReceived(byte[] previewData) {

        }

        @Override
        public void onStreamPreviewReceived(byte[] previewData) {

        }
    };

    public static CameraFragment createInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentCameraBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false);

        mCameraView = (CameraView) binding.getRoot().findViewById(R.id.camera_view);
        mCameraView.setSettingsModel(SharedPreferencesUtil.getLastCachedSettings(getContext()));
        mCameraView.setCameraStateListener(mCameraStateListener);
        mCameraView.setCameraCaptureListener(mCameraCaptureListener);

        binding.takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.takePicture();
            }
        });
        return binding.getRoot();
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
}
