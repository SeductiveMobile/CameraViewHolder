package com.seductive.tools.cameraholder.sample.ui.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.seductive.tools.cameraholder.sample.BR;

public abstract class ControllerViewModel extends BaseObservable implements ControllerTabsHandler {

    public enum TAB {
        CAMERA_SETTINGS, CAMERA
    }

    private TAB mCurrTab;

    public ControllerViewModel init(TAB tab) {
        mCurrTab = tab;
        return this;
    }

    @Override
    public final void onCameraSettingsSelected(View view) {
        selectTab(TAB.CAMERA_SETTINGS);
    }

    @Override
    public final void onCameraSelected(View view) {
        selectTab(TAB.CAMERA);
    }

    public void selectTab(TAB tab) {
        mCurrTab = tab;
        onTabSelected(tab);
        notifyPropertyChanged(BR.selectedTab);
    }

    @Bindable
    public int getSelectedTab() {
        return mCurrTab.ordinal();
    }

    protected abstract void onTabSelected(TAB tab);
}
