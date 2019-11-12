package com.sensorsdata.android.push;

import android.util.Log;

public class SFLogger {
    private static boolean isDebug = false;

    public static void setIsDebug(boolean isDebug) {
        SFLogger.isDebug = isDebug;
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }
}
