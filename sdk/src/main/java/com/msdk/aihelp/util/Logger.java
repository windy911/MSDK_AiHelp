package com.msdk.aihelp.util;

import android.util.Log;

public class Logger {

    private static final String TAG = "MSDKAiHelp";
    private static boolean enabled = true;

    public static void setEnabled(boolean enabled) {
        Logger.enabled = enabled;
    }

    public static void d(String message) {
        if (enabled) Log.d(TAG, message);
    }

    public static void i(String message) {
        if (enabled) Log.i(TAG, message);
    }

    public static void w(String message) {
        if (enabled) Log.w(TAG, message);
    }

    public static void e(String message, Throwable t) {
        if (enabled) Log.e(TAG, message, t);
    }
}
