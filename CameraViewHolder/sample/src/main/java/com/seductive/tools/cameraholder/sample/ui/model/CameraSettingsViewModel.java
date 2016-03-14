package com.seductive.tools.cameraholder.sample.ui.model;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;

import com.seductive.tools.cameraholder.model.SettingsModel;
import com.seductive.tools.cameraholder.sample.BR;
import com.seductive.tools.cameraholder.sample.ui.listeners.CustomOnSeekBarChangeListener;
import com.seductive.tools.cameraholder.sample.utils.SharedPreferencesUtil;
import com.seductive.tools.cameraholder.utils.CameraUtils;

import java.util.ArrayList;

public class CameraSettingsViewModel extends BaseObservable {

    public static final String TAG_FRONT_CAMERA = "TAG_FRONT_CAMERA";
    public static final String TAG_BACK_CAMERA = "TAG_BACK_CAMERA";
    public static final String TAG_SEEK_BAR_FOCUS = "TAG_SEEK_BAR_FOCUS";

    private Context mContext;
    private SettingsModel mModel;
    private boolean frontCameraAvailable;
    private ArrayList<String> frontFaceCameraResolutions = new ArrayList<>();
    private ArrayList<String> backFaceCameraResolutions = new ArrayList<>();
    private int selection;

    public CameraSettingsViewModel(Context context, SettingsModel model) {
        this.mModel = model;
        this.mContext = context;
        this.selection = getSelectionIndex();
        this.frontCameraAvailable = CameraUtils.isFrontFacingCameraAvailable();
        this.backFaceCameraResolutions = (ArrayList<String>) SharedPreferencesUtil.getBackFacingCameraResolutions(mContext);
        this.frontFaceCameraResolutions = (ArrayList<String>) SharedPreferencesUtil.getFrontFacingCameraResolutions(mContext);

    }

    public int getSelectionIndex() {
        int resolutionWidth = SharedPreferencesUtil.getResolutionWidth(mContext);
        int resolutionHeight = SharedPreferencesUtil.getResolutionHeight(mContext);
        if (resolutionWidth > 0 && resolutionHeight > 0) {
            String resolutionStr = resolutionWidth + "x" + resolutionHeight;
            return isCameraTypeFront() ? frontFaceCameraResolutions.indexOf(resolutionStr) :
                    backFaceCameraResolutions.indexOf(resolutionStr);
        }
        return 0;
    }

    public boolean areResolutionsListsReady() {
        return backFaceCameraResolutions.size() != 0 && (!CameraUtils.isFrontFacingCameraAvailable() ||
                frontFaceCameraResolutions.size() != 0);
    }

    @Bindable
    public int getSelection() {
        return selection;
    }

    public void setSelection(int selection) {
        this.selection = selection;
        this.notifyPropertyChanged(BR.selection);
    }

    public void setCameraTypeFront() {
        mModel.setCameraType(SettingsModel.CAMERA_TYPE.FRONT);
//        PreferencesUtils.setCameraTypeSelection(mContext, SettingsModel.CAMERA_TYPE.FRONT.ordinal());
        setSelection(0);
//        PreferencesUtils.setResolution(mContext, frontFaceCameraResolutions.get(0));
        notifyPropertyChanged(BR.frontFaceCameraResolutions);
        notifyPropertyChanged(BR.cameraFocusAvailable);
    }

    public void setCameraTypeBack() {
        mModel.setCameraType(SettingsModel.CAMERA_TYPE.BACK);
//        PreferencesUtils.setCameraTypeSelection(mContext, SettingsModel.CAMERA_TYPE.BACK.ordinal());
        setSelection(0);
//        PreferencesUtils.setResolution(mContext, backFaceCameraResolutions.get(0));
        notifyPropertyChanged(BR.backFaceCameraResolutions);
        notifyPropertyChanged(BR.cameraFocusAvailable);
    }

    @Bindable
    public boolean isCameraFocusAvailable(){
        if(mModel.getCameraType() == SettingsModel.CAMERA_TYPE.BACK) {
            return mModel.isBackCameraFocusAvailable();
        }
        return mModel.isFrontCameraFocusAvailable();
    }

    public boolean isCameraTypeFront() {
        return mModel.isCameraTypeFront();
    }

    public boolean isCameraTypeBack() {
        return mModel.isCameraTypeBack();
    }

    @Bindable
    public boolean getFrontCameraAvailable() {
        return frontCameraAvailable;
    }

    @Bindable
    public ArrayList<String> getFrontFaceCameraResolutions() {
        return frontFaceCameraResolutions;
    }

    @Bindable
    public ArrayList<String> getBackFaceCameraResolutions() {
        return backFaceCameraResolutions;
    }

    @Bindable
    public float getFocus() {
        return this.mModel.getFocus();
    }

    public void setFocus(int intFocusValue) {
        float focus = Float.parseFloat(String.valueOf(intFocusValue)) / 10;
        mModel.setFocus(focus);

        notifyPropertyChanged(BR.focus);
    }

    public AdapterView.OnItemSelectedListener getResolutionChangeListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String resolution;
                if (isCameraTypeFront()) {
                    resolution = frontFaceCameraResolutions.get(position);
                } else {
                    resolution = backFaceCameraResolutions.get(position);
                }
//                SharedPreferencesUtil.setResolution(mContext, resolution);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public void onClickView(View view) {
        notifyModelOnClickView((String) view.getTag());
    }

    private void notifyModelOnClickView(String tag) {
//        updateViewByTag(tag, null);
    }

    public CustomOnSeekBarChangeListener seekBarChangeListener = new CustomOnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //updateViewByTag((String) seekBar.getTag(), progress);
        }
    };
}
