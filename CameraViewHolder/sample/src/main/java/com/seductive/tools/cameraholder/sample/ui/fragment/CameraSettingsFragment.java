package com.seductive.tools.cameraholder.sample.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seductive.tools.cameraholder.model.Settings;
import com.seductive.tools.cameraholder.sample.R;
import com.seductive.tools.cameraholder.sample.databinding.FragmentCameraSettingsBinding;
import com.seductive.tools.cameraholder.sample.ui.model.CameraSettingsViewModel;
import com.seductive.tools.cameraholder.sample.utils.SharedPreferencesUtil;
import com.seductive.tools.cameraholder.utils.UIUtils;

public class CameraSettingsFragment extends Fragment {

    public static CameraSettingsFragment createInstance() {
        return new CameraSettingsFragment();
    }

    private CameraSettingsViewModel mSettingsViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentCameraSettingsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_settings, container, false);
        Settings settings = SharedPreferencesUtil.getLastCachedSettings(getActivity());
        mSettingsViewModel = new CameraSettingsViewModel(getActivity(), settings);
        binding.setSettings(mSettingsViewModel);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mSettingsViewModel.areResolutionsListsReady()) {
            UIUtils.showToastMsg(getActivity(), getResources().getString(R.string.no_resolutions_exception_msg));
        }
    }
}
