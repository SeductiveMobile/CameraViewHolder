package com.seductive.tools.cameraholder.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public final class SharedPreferencesUtil {

    private static final String PREF_NAME = "CameraSampleApp";

    private static final String STARTED_PARAM = "started_param";
    private static final String FRONT_CAMERA_RESOLUTION = "front_resolution";
    private static final String BACK_CAMERA_RESOLUTION = "back_resolution";
    private static final String BACK_CAMERA_FOCUS_AVAILABLE = "back_focus_available";

    private static final String CACHED_RESOLUTION_WIDTH = "resolution_width";
    private static final String CACHED_RESOLUTION_HEIGHT = "resolution_height";

    public static boolean alreadyStartedApp(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(STARTED_PARAM, false);
    }

    public static void setAlreadyStartedAppParam(Context context) {
        getEditor(context).putBoolean(STARTED_PARAM, true).apply();
    }


    public static void setBackFacingCameraResolutions(Context context, List<String> resolutions) {
        getEditor(context).putString(BACK_CAMERA_RESOLUTION, new Gson().toJson(resolutions)).apply();
    }

    public static void setFrontFacingCameraResolutions(Context context, List<String> resolutions) {
        getEditor(context).putString(FRONT_CAMERA_RESOLUTION, new Gson().toJson(resolutions)).apply();
    }

    public static List<String> getBackFacingCameraResolutions(Context context) {
        String retrievedJson = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(BACK_CAMERA_RESOLUTION, "");
        if (retrievedJson.isEmpty()) {
            return new ArrayList<>();
        } else return new Gson().fromJson(retrievedJson, new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    public static void setBackCameraFocusAvailable(Context context, boolean available) {
        getEditor(context).putBoolean(BACK_CAMERA_FOCUS_AVAILABLE, available).apply();
    }

    public static List<String> getFrontFacingCameraResolutions(Context context) {
        String retrievedJson = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(FRONT_CAMERA_RESOLUTION, "");
        if (retrievedJson.isEmpty()) {
            return new ArrayList<>();
        } else return new Gson().fromJson(retrievedJson, new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    public static int getResolutionWidth(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(CACHED_RESOLUTION_WIDTH, 0);
    }

    public static int getResolutionHeight(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(CACHED_RESOLUTION_HEIGHT, 0);
    }

    /**
     * Common shared preferences editor to write changes
     *
     * @param context
     * @return
     */
    private static SharedPreferences.Editor getEditor(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
    }
}
