package com.upbad.apps.vogo.plugin;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.upbad.apps.vogo.util.HookParams.NOTIFICATION_BUILDER_ACTION_LIST;
import static com.upbad.apps.vogo.util.ReflectionUtil.log;

public class NotificationActions implements IPlugin {

    PendingIntent pendingIntent = null;

    @Override
    public void hook(final XC_LoadPackage.LoadPackageParam lpparam, final ClassLoader classLoader) {
        log("NotificationActions hook() started");
        // Reverse engineer tip:
        //   - search "notify" for the use of `Notification` class
        //   - search "addAction" for adding actions (buttons on the pop-up)
        //   - packages start with `defpackage` should be named without this prefix
        //   - search "setRemoteInputHistory" or "actionIntent" for the notification generator

        // There is a bundle in the notification generator
        // that contains a hidden PendingIntent (probably used for Android Auto),
        // which can be directly used to mark the message as read.

        // Hook the unique statement that puts the hidden PendingIntent in a bundle
        XposedHelpers.findAndHookMethod("android.os.Bundle", classLoader, "putParcelable", String.class, Parcelable.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (param.args[0].equals("actionIntent")) {
                        pendingIntent = (PendingIntent) param.args[1];
                    }
                } catch (Error | Exception e) {
                    log(e);
                }
            }
        });

        // Hook the unique statement that invokes a function of the Notification.Builder in the notification generator
        XposedHelpers.findAndHookMethod("android.app.Notification.Builder", classLoader, "setRemoteInputHistory", CharSequence[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    Object notificationBuilder = param.thisObject;
                    ArrayList<Notification.Action> actions = (ArrayList<Notification.Action>) XposedHelpers.getObjectField(notificationBuilder, NOTIFICATION_BUILDER_ACTION_LIST);
                    if (actions.size() != 1) {
                        return;
                    }

                    if (pendingIntent == null) {
                        return;
                    }
                    Notification.Action.Builder actionBuilder = new Notification.Action.Builder(0, "Mark as Read", pendingIntent);
                    XposedHelpers.callMethod(notificationBuilder, "addAction", actionBuilder.build());
                } catch (Error | Exception e) {
                    log(e);
                }
            }
        });

        log("NotificationActions hook() finished");
    }
}
