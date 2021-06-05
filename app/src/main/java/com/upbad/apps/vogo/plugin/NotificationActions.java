package com.upbad.apps.vogo.plugin;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.upbad.apps.vogo.util.HookParams.NOTIFICATION_BUILDER_ACTION_LIST;
import static com.upbad.apps.vogo.util.HookParams.VOICE_NOTIFICATION_ATTRIBUTE_CLASS_CAR_BUNDLE_GETTER;
import static com.upbad.apps.vogo.util.HookParams.VOICE_NOTIFICATION_GENERATOR_CLASS;
import static com.upbad.apps.vogo.util.HookParams.VOICE_NOTIFICATION_ATTRIBUTE_CLASS;
import static com.upbad.apps.vogo.util.HookParams.VOICE_NOTIFICATION_GENERATOR_CLASS_NOTIFICATION_BUILDER_FIELD;
import static com.upbad.apps.vogo.util.ReflectionUtil.log;

public class NotificationActions implements IPlugin {
    @Override
    public void hook(final XC_LoadPackage.LoadPackageParam lpparam, final ClassLoader classLoader) {
        log("NotificationActions hook() started");
        // Reverse engineer tip:
        //   - search for "notify" for use of `Notification` class
        //   - search for "addAction" for adding actions (buttons on the pop-up)
        //   - packages start with `defpackage` should be named without this prefix
        // XposedBridge.hookAllConstructors may also be used since there's only one constructor in this class
        XposedBridge.hookMethod(XposedHelpers.findConstructorExact(VOICE_NOTIFICATION_GENERATOR_CLASS, classLoader, VOICE_NOTIFICATION_ATTRIBUTE_CLASS), new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    Object notificationBuilder = XposedHelpers.getObjectField(param.thisObject, VOICE_NOTIFICATION_GENERATOR_CLASS_NOTIFICATION_BUILDER_FIELD);
                    ArrayList<Notification.Action> actions = (ArrayList<Notification.Action>) XposedHelpers.getObjectField(notificationBuilder, NOTIFICATION_BUILDER_ACTION_LIST);
                    if (actions.size() != 1) {
                        return;
                    }
                    // There is bundle contained in the first (and only) parameter object.
                    // This bundle contains a hidden PendingIntent (probably used for Android Auto),
                    // which can be directly used to mark the message as read.
                    // This bundle can be obtained using the integrated getter below, or directly via `getObjectField`
                    Bundle bundle = (Bundle) XposedHelpers.callMethod(param.args[0], VOICE_NOTIFICATION_ATTRIBUTE_CLASS_CAR_BUNDLE_GETTER);
                    PendingIntent pendingIntent = bundle.getBundle("android.car.EXTENSIONS").getBundle("invisible_actions").getBundle("0").getParcelable("actionIntent");
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
