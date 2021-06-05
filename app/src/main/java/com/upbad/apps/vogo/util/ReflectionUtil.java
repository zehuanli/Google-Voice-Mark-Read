package com.upbad.apps.vogo.util;


import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public final class ReflectionUtil {

    private static boolean xposedExist;

    static {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge");
            xposedExist = true;
        } catch (ClassNotFoundException e) {
            xposedExist = false;
            log(e);
        }
    }

    public static void log(String msg) {
        if (msg == null) {
            return;
        }
        if (xposedExist) {
            XposedBridge.log("[VOGO] " + msg);
        } else {
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
