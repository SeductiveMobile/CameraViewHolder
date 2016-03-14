package com.seductive.tools.cameraholder.sample.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seductive.tools.cameraholder.sample.R;
import com.seductive.tools.cameraholder.sample.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {

    public static CameraFragment createInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentCameraBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false);

        return binding.getRoot();
    }
}
