package com.seductive.tools.cameraholder.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Toast;

import java.util.HashMap;

public final class UIUtils {
    private static Toast toast;

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static void showToastMsg(Context context, String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void cancelToastMsg() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
