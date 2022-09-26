package com.upbad.apps.vogo.util;


import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public final class LogUtil {

    public static void log(String msg) {
        if (msg == null) {
            return;
        }
        try {
            XposedBridge.log("[VOGO] " + msg);
        } catch (Exception unused) {
            Log.i("VOGO", msg);
        }
    }

    public static void log(Throwable e) {
        if (e == null) {
            return;
        }
        log(Log.getStackTraceString(e));
    }

}
