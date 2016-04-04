package com.seductive.tools.cameraholder.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Provides some functionality for interaction with UI: showing or canceling little messages,
 * receiving of screen resolution
 */
public final class UIUtils {
    /**
     *
     */
    private static Toast toast;

    /**
     * Method for receiving screen width using {@link android.util.DisplayMetrics}
     *
     * @param context current Context instance
     * @return screen width
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * Method for receiving screen height using {@link android.util.DisplayMetrics}
     *
     * @param context current Context instance
     * @return screen height
     */
    public static int getScreenHeight(Context context) {
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
        return point.y;
    }

    /**
     * Creates new Toast instance and shows it for {@link Toast#LENGTH_LONG} time.
     * If Toast is already shown - it should be cancelled, created new instance and showed it
     *
     * @param context current Context instance
     * @param message Short message to show
     */
    public static void showToastMsg(Context context, String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * If Toast message is shown, method hides and cancels it
     */
    public static void cancelToastMsg() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
