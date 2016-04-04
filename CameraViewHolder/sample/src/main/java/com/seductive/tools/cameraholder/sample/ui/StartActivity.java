package com.seductive.tools.cameraholder.sample.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.seductive.tools.cameraholder.sample.R;
import com.seductive.tools.cameraholder.sample.databinding.ActivityStartBinding;
import com.seductive.tools.cameraholder.sample.event.InitializationEvent;
import com.seductive.tools.cameraholder.sample.service.InitializationService;
import com.seductive.tools.cameraholder.sample.ui.fragment.CameraFragment;
import com.seductive.tools.cameraholder.sample.ui.fragment.CameraSettingsFragment;
import com.seductive.tools.cameraholder.sample.ui.fragment.StartFragment;
import com.seductive.tools.cameraholder.sample.ui.fragment.listeners.OnCameraSettingsAnimationListener;
import com.seductive.tools.cameraholder.sample.ui.fragment.listeners.OnStartBtnClickListener;
import com.seductive.tools.cameraholder.sample.ui.model.ControllerViewModel;
import com.seductive.tools.cameraholder.sample.utils.SharedPreferencesUtil;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class StartActivity extends AppCompatActivity implements OnStartBtnClickListener,
        OnCameraSettingsAnimationListener {

    private static final int CAMERA_REQUEST_CODE = 0;

    public enum APP_STATE {
        START, CAMERA_SETTINGS, CAMERA
    }

    private final ControllerViewModel mViewModel = new ControllerViewModel() {
        @Override
        protected void onTabSelected(ControllerViewModel.TAB tab) {
            switch (tab) {
                case CAMERA_SETTINGS:
                    updateAppState(APP_STATE.CAMERA_SETTINGS);
                    break;
                case CAMERA:
                    updateAppState(APP_STATE.CAMERA);
                    break;
            }
        }
    };

    private APP_STATE mCurrState;
    private ActivityStartBinding mBindingObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBindingObject = DataBindingUtil.setContentView(this, R.layout.activity_start);
        if (SharedPreferencesUtil.alreadyStartedApp(this)) {
            checkIfPermissionsAreSet();
        } else {
            updateAppState(APP_STATE.START);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onStartBtnClick() {
        checkIfPermissionsAreSet();
    }

    @Override
    public void onAnimationFinished() {
        mBindingObject.activityMainBottomTabs.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length == 1) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(mBindingObject.container,
                            getString(R.string.camera_permission_not_granted_msg), Snackbar.LENGTH_SHORT).show();
                    notifyFragmentHideSettingsLoading();
                } else {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            continueAfterPermissionSet();
                        }
                    }, 0);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onEventMainThread(InitializationEvent event) {
        mBindingObject.setModel(mViewModel.init(ControllerViewModel.TAB.CAMERA_SETTINGS));
        mViewModel.selectTab(ControllerViewModel.TAB.CAMERA_SETTINGS);
    }

    private void notifyFragmentHideSettingsLoading() {
        if (mCurrState == null) return;
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(mCurrState.name());
        if (currentFragment != null && currentFragment instanceof StartFragment) {
            ((StartFragment) currentFragment).hideLoadingDueToError();
        }
    }

    private void checkIfPermissionsAreSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestForCameraPermission();
        } else {
            continueAfterPermissionSet();
        }
    }

    private void requestForCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                CAMERA_REQUEST_CODE);
    }

    private void continueAfterPermissionSet() {
        if (!SharedPreferencesUtil.alreadyStartedApp(this)) {
            SharedPreferencesUtil.setAlreadyStartedAppParam(this);
        }
        startService(new Intent(this, InitializationService.class));
    }

    private void updateAppState(APP_STATE newState) {
        if (mCurrState == newState)
            return;
        Fragment fragment = null;
        switch (newState) {
            case START:
                fragment = StartFragment.createInstance();
                mBindingObject.activityMainBottomTabs.setVisibility(View.GONE);
                break;
            case CAMERA_SETTINGS:
                fragment = CameraSettingsFragment.createInstance();
                break;
            case CAMERA:
                fragment = CameraFragment.createInstance();
                break;
        }
        int appearanceAnimation = R.anim.slide_in_right;
        int disappearanceAnimation = R.anim.slide_out_left;
        if (mCurrState != null && mCurrState.ordinal() > newState.ordinal()) {
            appearanceAnimation = R.anim.slide_in_left;
            disappearanceAnimation = R.anim.slide_out_right;
        }
        mCurrState = newState;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(appearanceAnimation, disappearanceAnimation);
        ft.replace(R.id.container, fragment, mCurrState.name());
        ft.commit();
    }
}
